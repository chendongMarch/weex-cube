package com.march.wxcube

import android.content.Context
import android.os.Build
import android.util.LruCache
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.memory
import com.march.wxcube.common.report
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.taobao.weex.utils.WXFileUtils
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/3/27
 * Describe : 对 page 对应的 template 做缓存，加速访问，防止频繁 IO
 *
 * @author chendong
 */
class WeexJsLoader(context: Context, jsLoadStrategy: Int, jsCacheStrategy: Int, jsPrepareStrategy: Int) : WeexUpdater.UpdateHandler {

    companion object {
        private val TAG = WeexJsLoader::class.java.simpleName!!
        const val CACHE_DIR = "weex-js-disk-cache"
        const val DISK_MAX_SIZE = 20 * 1024 * 1024L
    }

    private var fromWhere: String = ""
    // 线程池
    private val mService: ExecutorService = Executors.newFixedThreadPool(1)
    // 加载策略
    private val mJsLoadStrategy = jsLoadStrategy
    // 缓存策略
    private val mJsCacheStrategy = jsCacheStrategy
    // 预加载策略
    private val mJsPrepareStrategy = jsPrepareStrategy

    // 内存缓存
    private val mJsMemoryCache = JsMemoryCache(context.memory(.3f))
    // 文件缓存
    private val mJsFileCache = JsFileCache(Weex.getInst().makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)

    override fun onUpdateConfig(context: Context, weexPages: List<WeexPage>?) {
        if (mJsPrepareStrategy == JsPrepareStrategy.PREPARE_ALL) {
            weexPages?.forEach { getTemplateAsync(context, it) {} }
        }
    }

    /**
     * 异步获取模板
     * 使用默认的加载和缓存策略
     */
    fun getTemplateAsync(context: Context, page: WeexPage?, consumer: (String?) -> Unit) {
        getTemplateAsync(context, mJsLoadStrategy, mJsCacheStrategy, page, consumer)
    }

    /**
     * 异步获取模板
     * @param context Ctx
     * @param loadStrategy 加载策略
     * @param cacheStrategy 缓存策略
     * @param page 页面数据
     * @param consumer 处理结果的函数
     */
    fun getTemplateAsync(context: Context, loadStrategy: Int, cacheStrategy: Int, page: WeexPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        val publishFunc: (String?) -> Unit = {
            consumer(it)
            if (cacheStrategy != JsCacheStrategy.NO_CACHE) {
                it?.let {
                    mJsMemoryCache.checkPut(page.key, it)
                }
            }
        }
        val runnable = if (loadStrategy != JsLoadStrategy.DEFAULT) {
            makeJsLoader(loadStrategy, context, page)
        } else {
            {
                var template: String? = ""
                if (template.isNullOrBlank()) {
                    template = makeJsLoader(JsLoadStrategy.CACHE_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.assetsJs.isNullOrBlank()) {
                    template = makeJsLoader(JsLoadStrategy.ASSETS_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.localJs.isNullOrBlank()) {
                    template = makeJsLoader(JsLoadStrategy.FILE_FIRST, context, page)()
                }
                if (template.isNullOrBlank() && !page.remoteJs.isNullOrBlank()) {
                    template = makeJsLoader(JsLoadStrategy.NET_FIRST, context, page)()
                }
                template
            }
        }
        mService.execute {
            val template = runnable.invoke()
            Weex.getInst().mWeexInjector.onLog(TAG, "JS加载${page.pageName} cache[${mJsMemoryCache.size()}] $fromWhere")
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
        val http = ManagerRegistry.REQ
        val makeJsResUrl = ManagerRegistry.HOST.makeJsResUrl(url)
        val wxRequest = http.makeWxRequest(url = makeJsResUrl, from = "download-js")
        val resp = http.requestSync(wxRequest, false)
        if (page.localJs != null && resp.data != null && mJsCacheStrategy == JsCacheStrategy.CACHE_MEMORY_DISK_BOTH) {
            mJsFileCache.write(page.localJs!!, resp.data)
        }
        return resp.data
    }

    // 加载函数
    private fun makeJsLoader(type: Int, context: Context, page: WeexPage): () -> String? {
        return when (type) {
            JsLoadStrategy.CACHE_FIRST -> {
                {
                    fromWhere = "缓存"
                    mJsMemoryCache.get(page.key)
                }
            }
            JsLoadStrategy.ASSETS_FIRST -> {
                {
                    if (isAssetsExist(page.assetsJs ?: "", context)) {
                        fromWhere = "assets"
                        WXFileUtils.loadAsset("js/${page.assetsJs}", context)
                    } else {
                        ""
                    }
                }
            }
            JsLoadStrategy.FILE_FIRST -> {
                {
                    fromWhere = "文件"
                    page.localJs?.let { mJsFileCache.read(it) }
                }
            }
            JsLoadStrategy.NET_FIRST -> {
                {
                    fromWhere = "网络"
                    downloadJs(page)
                }
            }
            else -> {
                { "" }
            }
        }
    }


    private fun isAssetsExist(name: String, context: Context): Boolean {
        return try {
            val files = context.assets.list("js")
            files.any { name == it }
        } catch (e: Exception) {
            false
        }
    }
}

// js 内存缓存
class JsMemoryCache(maxSize: Int) : LruCache<String, String>(maxSize) {
    override fun sizeOf(key: String, value: String): Int {
        return value.length
    }

    fun checkPut(key: String?, value: String?) {
        if (key.isNullOrBlank()) {
            report("JsMemCache key is empty $key")
            return
        }
        if (value.isNullOrBlank()) {
            report("JsMemCache value is empty $value")
            return
        }
        put(key, value)
    }
}

// js 文件缓存
class JsFileCache(dir: File, maxSize: Long) : DiskLruCache(dir, maxSize)

// 加载策略
object JsLoadStrategy {
    const val NET_FIRST = 0 // 只加载网络
    const val FILE_FIRST = 1 // 只加载文件
    const val ASSETS_FIRST = 2 // 只加载 assets
    const val CACHE_FIRST = 3 // 只加载缓存
    const val DEFAULT = 4 // 默认 缓存、文件、assets、网络 一次检查
}

// 预加载策略
object JsPrepareStrategy {
    const val PREPARE_ALL = 0 // 提前准备所有的js到缓存中
    const val LAZY_LOAD = 1 // 使用时才加载
    const val NO_CACHE = 2 // 不缓存
}

// 缓存策略
object JsCacheStrategy {
    const val NO_CACHE = 0 // 不缓存，加载后仅使用一次，下次仍旧从原资源加载
    const val CACHE_MEMORY_ONLY = 1 // 仅缓存到内存中
    const val CACHE_MEMORY_DISK_BOTH = 2 // 内存和磁盘都缓存
}
