package com.march.wxcube

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.LruCache
import com.march.common.disklru.DiskLruCache
import com.march.wxcube.manager.ManagerRegistry

import com.march.wxcube.model.WeexPage
import com.taobao.weex.utils.WXFileUtils
import java.io.Closeable
import java.io.File

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/3/27
 * Describe : 对 page 对应的 template 做缓存，加速访问，防止频繁 IO
 *
 * @author chendong
 */
class WeexJsLoader(config: WeexConfig) {

    // 线程池
    private val mService: ExecutorService = Executors.newFixedThreadPool(1)
    // 加载策略
    private val mJsLoadStrategy = config.jsLoadStrategy
    // 缓存策略
    private val mJsCacheStrategy = config.jsCacheStrategy
    // 内存缓存
    private val mJsMemoryCache = config.jsMemoryCache ?: let {
        val size = config.jsMemoryCacheMaxSize ?: let {
            val activityManager = config.application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            (activityManager.memoryClass * 1024 * 1024 * 0.3f).toInt()
        }
        WeexJsLoader.JsMemoryCache(size)
    }
    // 文件缓存
    private val mJsFileCache = config.jsFileCache ?: let {
        WeexJsLoader.JsFileCache(File(config.jsFileCacheDir
                ?: config.application.cacheDir, WeexJsLoader.CACHE_DIR),
                config.jsFileCacheMaxSize ?: 30 * 1024 * 1024)
    }

    interface IJsFileCache : Closeable {
        fun read(key: String): String?
        fun write(key: String, value: String?)
    }

    fun update(context: Context, weexPages: List<WeexPage>) {
        if (mJsCacheStrategy == JsCacheStrategy.PREPARE_ALL) {
            for (page in weexPages) {
                getTemplateAsync(context, page) {}
            }
        }
    }

    fun getTemplateAsync(context: Context, page: WeexPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        val publishFunc: (String?) -> Unit = {
            if (it.isNullOrBlank()) {
                consumer(null)
            } else {
                if (mJsCacheStrategy != JsCacheStrategy.NO_CACHE) {
                    mJsMemoryCache.put(page, it)
                }
                consumer(it)
            }
        }
        var fromWhere = ""
        val netLoader by lazy {
            {
                fromWhere = "网络"
                downloadJs(page)
            }
        }
        val assetsLoader by lazy {
            {
                fromWhere = "assets"
                WXFileUtils.loadAsset(page.assetsJs, context)
            }
        }
        val fileLoader by lazy {
            {
                fromWhere = "文件"
                page.localJs?.let { mJsFileCache.read(it) }
            }
        }
        val cacheLoader by lazy { { mJsMemoryCache.get(page) } }
        val defaultLoader by lazy {
            {
                var template: String? = ""
                if (template.isNullOrBlank()) {
                    template = cacheLoader()
                    fromWhere = "缓存"
                }
                if (template.isNullOrBlank() && !page.localJs.isNullOrBlank()) {
                    template = fileLoader()
                    fromWhere = "文件"
                }
                if (template.isNullOrBlank() && !page.assetsJs.isNullOrBlank()) {
                    template = assetsLoader()
                    fromWhere = "assets"
                }
                if (template.isNullOrBlank() && !page.remoteJs.isNullOrBlank()) {
                    fromWhere = "网络"
                    template = netLoader()
                }
                template
            }
        }
        val runnable = when (mJsLoadStrategy) {
            JsLoadStrategy.NET_FIRST -> netLoader // 只加载网络
            JsLoadStrategy.ASSETS_FIRST -> assetsLoader // 只加载 assets
            JsLoadStrategy.FILE_FIRST -> fileLoader // 只加载文件
            JsLoadStrategy.CACHE_FIRST -> cacheLoader // 只加载缓存
            JsLoadStrategy.DEFAULT -> defaultLoader  // 默认 缓存 -> 文件 -> assets -> 网络
            else -> defaultLoader
        }
        mService.execute {
            val template = runnable.invoke()
            log("JS加载${page.pageName} cache[${mJsMemoryCache.size()}] $fromWhere")
            publishFunc(template)
        }
    }

    fun clearCache() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mJsMemoryCache.trimToSize(-1)
        }
    }

    private fun downloadJs(page: WeexPage): String? {
        val url = page.remoteJs ?: return null
        val http = ManagerRegistry.HTTP
        val wxRequest = http.makeWxRequest(url = url, from = "download-js")
        val resp = http.requestSync(wxRequest, false)
        page.localJs?.let { mJsFileCache.write(it, resp.data) }
        return resp.data
    }


    object JsLoadStrategy {
        const val NET_FIRST = 0 // 只加载网络
        const val FILE_FIRST = 1 // 只加载文件
        const val ASSETS_FIRST = 2 // 只加载 assets
        const val CACHE_FIRST = 3 // 只加载缓存
        const val DEFAULT = 4 // 默认 缓存、文件、assets、网络 一次检查
    }

    object JsCacheStrategy {
        const val PREPARE_ALL = 0 // 提前准备所有的js到缓存中
        const val LAZY_LOAD = 2 // 使用时才加载
        const val NO_CACHE = 3 // 不缓存
    }

    companion object {
        private val TAG = WeexJsLoader::class.java.simpleName!!
        const val CACHE_DIR = "weex-js-disk-cache"
        fun log(msg: String) {
            Weex.getInst().mWeexInjector.onLog(TAG, msg)
        }

    }

    class JsMemoryCache(maxNum: Int) : LruCache<WeexPage, String>(maxNum) {

        override fun sizeOf(key: WeexPage, value: String): Int {
            return value.length
        }
    }

    class JsFileCache(dir: File, maxSize: Long) : WeexJsLoader.IJsFileCache {

        private val diskCache by lazy { DiskLruCache.open(dir, 1, 1, maxSize) }

        override fun read(key: String): String? {
            val result = diskCache.get(key)?.getString(0)
            return result
        }

        override fun write(key: String, value: String?) {
            val edit = diskCache.edit(key)
            edit?.set(0, value)
            edit?.commit()
        }

        override fun close() {
            if (!diskCache.isClosed) {
                diskCache.close()
            }
        }
    }

}
