package com.march.wxcube.func.loader

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.LruCache
import com.march.common.model.WeakContext
import com.march.common.utils.LgUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.adapter.IWxReportAdapter
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.log
import com.march.wxcube.func.update.OnWxUpdateListener
import com.march.wxcube.model.WxPage
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.coroutines.experimental.bg
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/3/27
 * Describe : 对 page 对应的 template 做缓存，加速访问，防止频繁 IO
 *
 * @author chendong
 */
class WxJsLoader : OnWxUpdateListener {

    companion object {
        private const val TAG = "WxJsLoader"
        const val CACHE_DIR = "weex-js-disk-cache"
        const val DISK_MAX_SIZE = 20 * 1024 * 1024L
    }

    // 线程池
    private val mService: ExecutorService = Executors.newCachedThreadPool()
    // 内存缓存
    private val mJsMemoryCache by lazy {
        JsMemoryCache(let {
            val ctx = CubeWx.mWeakCtx.get()
            if (ctx == null) {
                5 * 1024 * 1024
            } else {
                val memoryRatio = .3
                val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                (activityManager.memoryClass * 1024 * 1024 * memoryRatio).toInt()
            }
        })
    }
    // 文件缓存
    private val mJsFileCache by lazy {
        JsFileCache(WxUtils.makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }
    // 资源加载器
    private val mLoaderRegistry by lazy {
        mapOf(JsLoadStrategy.CACHE_FIRST to CacheResourceLoader(mJsMemoryCache),
                JsLoadStrategy.ASSETS_FIRST to AssetsResourceLoader(),
                JsLoadStrategy.FILE_FIRST to FileResourceLoader(mJsFileCache),
                JsLoadStrategy.NET_FIRST to NetResourceLoader())
    }

    override fun onWeexCfgUpdate(context: Context, weexPages: List<WxPage>?) {
        if (CubeWx.mWxCfg.jsPrepareStrategy == JsPrepareStrategy.PREPARE_ALL) {
            weexPages?.forEach {
                launch(UI) { getTemplate(context, it) }
            }
        }
    }

    /**
     * 异步获取模板
     * @param context Ctx
     * @param loadStrategy 加载策略
     * @param cacheStrategy 缓存策略
     * @param page 页面数据
     * @param consumer 处理结果的函数
     */
    fun getTemplateAsync(context: Context, loadStrategy: Int, cacheStrategy: Int, page: WxPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        mService.execute {
            // 同步获取模板数据
            val (realLoadStrategy, template) = getTemplateSync(WeakContext(context), loadStrategy, page)
            // 先返回数据给渲染线程
            consumer(template)
            // 缓存存储模板数据
            storeTemplate(realLoadStrategy, cacheStrategy, page, template)
        }
    }

    fun getTemplate(context: Context, page: WxPage?): Deferred<String?>? {
        val loadStrategy = CubeWx.mWxCfg.jsLoadStrategy
        val cacheStrategy = CubeWx.mWxCfg.jsCacheStrategy
        return getTemplate(context, loadStrategy, cacheStrategy,page)
    }


    fun getTemplate(context: Context?, loadStrategy: Int, cacheStrategy: Int, page: WxPage?): Deferred<String?>? {
        if (context == null || page == null) {
            return null
        }
        return bg {
            // 同步获取模板数据
            val (realLoadStrategy, template) = getTemplateSync(WeakContext(context), loadStrategy, page)
            // 缓存存储模板数据
            storeTemplate(realLoadStrategy, cacheStrategy, page, template)
            template
        }
    }

    // 同步加载数据信息
    private fun getTemplateSync(weakCtx: WeakContext, loadStrategy: Int, page: WxPage): Pair<Int, String?> {
        var realLoadStrategy = loadStrategy
        var template: String? = null
        // 指定方式加载
        if (loadStrategy != JsLoadStrategy.DEFAULT) {
            template = mLoaderRegistry[loadStrategy]?.load(weakCtx.get(), page)
        } else {
            val strategies = arrayOf(
                    JsLoadStrategy.CACHE_FIRST,
                    JsLoadStrategy.ASSETS_FIRST,
                    JsLoadStrategy.FILE_FIRST,
                    JsLoadStrategy.NET_FIRST)
            for (strategy in strategies) {
                realLoadStrategy = strategy
                template = mLoaderRegistry[strategy]?.load(weakCtx.get(), page)
                if (!template.isNullOrBlank()) break
            }
        }
        log("JS加载${page.pageName} cache[${mJsMemoryCache.size()}] ${JsLoadStrategy.desc(realLoadStrategy)}")
        return realLoadStrategy to template
    }

    // 同步缓存数据模板信息
    private fun storeTemplate(realLoadStrategy: Int, cacheStrategy: Int, page: WxPage, template: String?) {
        if (template == null || template.isBlank()) {
            CubeWx.mWxReportAdapter.report(IWxReportAdapter.CODE_JS_LOAD_ERROR, """
                    page = $page
                    loadStrategy = $realLoadStrategy
                    cacheStrategy = $cacheStrategy
                    templateSize = ${template?.length}
                """.trimIndent())
            return
        }
        // 不管从哪里取出来，都需要往内存中存
        if (cacheStrategy != JsCacheStrategy.NO_CACHE) {
            mJsMemoryCache.checkPut(page.key, template)
        }
        // 网络获取的考虑存文件
        if (realLoadStrategy == JsLoadStrategy.NET_FIRST
                && cacheStrategy == JsCacheStrategy.CACHE_MEMORY_DISK_BOTH) {
            // 调试模式没有 md5 || 正式模式有 md5
            val curMd5 = WxUtils.md5(template)
            val pageMd5 = page.md5
            LgUtils.e("md5 check ${page.pageName} $curMd5 $pageMd5")
            if (pageMd5.isBlank() || curMd5.equals(pageMd5, true)) {
                LgUtils.e("write page $page")
                mJsFileCache.write(page.localJs, template)
            } else {
                CubeWx.mWxReportAdapter.report(IWxReportAdapter.CODE_MD5_NOT_MATCH, """
                    $page
                    loadStrategy = $realLoadStrategy
                    cacheStrategy = $cacheStrategy
                    templateSize = ${template.length}
                """.trimIndent())
            }
        }
    }

    fun clearCache() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mJsMemoryCache.trimToSize(-1)
        }
    }

    fun isAssetsJsExist(context: Context?, page: WxPage): Boolean {
        if (context == null) {
            return false
        }
        return try {
            val files = context.assets.list("js")
            files.any { page.assetsJs == it }
        } catch (e: Exception) {
            false
        }
    }

    fun isLocalJsExist(page: WxPage): Boolean {
        val cacheDir = WxUtils.makeCacheDir(CACHE_DIR)
        val files = cacheDir.list()
        return files.any { it.startsWith(page.localJs) }
    }

    fun prepareRemoteJsSync(context: Context, pages: List<WxPage>) {
        if (pages.isEmpty()) {
            return
        }
        pages.forEach {
            val template = mLoaderRegistry[JsLoadStrategy.NET_FIRST]?.load(context, it)
            storeTemplate(JsLoadStrategy.NET_FIRST, JsCacheStrategy.CACHE_MEMORY_DISK_BOTH, it, template)
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
            log("JsMemCache key is empty $key")
            return
        }
        if (value.isNullOrBlank()) {
            log("JsMemCache value is empty $value")
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

    fun desc(strategy: Int): String {
        return when (strategy) {
            NET_FIRST    -> "网络"
            FILE_FIRST   -> "文件"
            ASSETS_FIRST -> "Assets"
            CACHE_FIRST  -> "缓存"
            DEFAULT      -> "默认"
            else         -> "未知"
        }
    }
}

// 预加载策略
object JsPrepareStrategy {
    const val PREPARE_ALL = 0 // 提前准备所有的js到缓存中
    const val LAZY_LOAD = 1 // 使用时才加载
}

// 缓存策略
object JsCacheStrategy {
    const val NO_CACHE = 0 // 不缓存，加载后仅使用一次，下次仍旧从原资源加载
    const val CACHE_MEMORY_ONLY = 1 // 仅缓存到内存中
    const val CACHE_MEMORY_DISK_BOTH = 2 // 内存和磁盘都缓存


}
