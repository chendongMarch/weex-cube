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
class WeexJsLoader(config: WeexConfig) : UpdateHandler {


    private var fromWhere: String = ""
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
        fun write(key: String, value: String)
    }


    override fun updateWeexPages(postIndex: Boolean, context: Context, pages: List<WeexPage>?) {
        if (mJsCacheStrategy == JsCacheStrategy.PREPARE_ALL) {
            pages?.forEach { getTemplateAsync(context, it) {} }
        }
    }

    fun getTemplateAsync(context: Context, page: WeexPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        val publishFunc: (String?) -> Unit = {
            consumer(it)
            if (mJsCacheStrategy != JsCacheStrategy.NO_CACHE) {
                mJsMemoryCache.put(page, it)
            }
        }
        val runnable = if (mJsLoadStrategy != JsLoadStrategy.DEFAULT) {
            newLoader(mJsLoadStrategy, context, page)
        } else {
            {
                var template: String? = ""
                if (template.isNullOrBlank()) {
                    template = newLoader(JsLoadStrategy.CACHE_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.localJs.isNullOrBlank()) {
                    template = newLoader(JsLoadStrategy.FILE_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.assetsJs.isNullOrBlank()) {
                    template = newLoader(JsLoadStrategy.ASSETS_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.remoteJs.isNullOrBlank()) {
                    template = newLoader(JsLoadStrategy.NET_FIRST, context, page)()
                }
                template
            }
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
        if (page.localJs != null && resp.data != null) {
            mJsFileCache.write(page.localJs!!, resp.data)
        }
        return resp.data
    }

    // 加载函数
    private fun newLoader(type: Int, context: Context, page: WeexPage): () -> String? {
        return when (type) {
            JsLoadStrategy.NET_FIRST -> {
                {
                    fromWhere = "网络"
                    downloadJs(page)
                }
            }
            JsLoadStrategy.ASSETS_FIRST -> {
                {
                    fromWhere = "assets"
                    WXFileUtils.loadAsset(page.assetsJs, context)
                }
            }
            JsLoadStrategy.FILE_FIRST -> {
                {
                    fromWhere = "文件"
                    page.localJs?.let { mJsFileCache.read(it) }
                }
            }
            JsLoadStrategy.CACHE_FIRST -> {
                {
                    fromWhere = "缓存"
                    mJsMemoryCache.get(page)
                }
            }
            else -> {
                { "" }
            }
        }
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
            return diskCache.get(key)?.getString(0)
        }

        override fun write(key: String, value: String) {
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

// 加载策略
object JsLoadStrategy {
    const val NET_FIRST = 0 // 只加载网络
    const val FILE_FIRST = 1 // 只加载文件
    const val ASSETS_FIRST = 2 // 只加载 assets
    const val CACHE_FIRST = 3 // 只加载缓存
    const val DEFAULT = 4 // 默认 缓存、文件、assets、网络 一次检查
}

// 缓存策略
object JsCacheStrategy {
    const val PREPARE_ALL = 0 // 提前准备所有的js到缓存中
    const val LAZY_LOAD = 2 // 使用时才加载
    const val NO_CACHE = 3 // 不缓存
}
