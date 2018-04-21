package com.march.wxcube

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.LruCache
import com.march.common.utils.FileUtils
import com.march.wxcube.manager.ManagerRegistry

import com.march.wxcube.model.WeexPage
import com.taobao.weex.utils.WXFileUtils

import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/3/27
 * Describe : 对 page 对应的 template 做缓存，加速访问，防止频繁 IO
 *
 * @author chendong
 */
class WeexJsLoader(loadStrategy: Int, prepareStrategy: Int, maxSize: Int) {

    private val mService: ExecutorService = Executors.newFixedThreadPool(1)
    private val mJsLoadStrategy: Int = loadStrategy
    private val mJsCacheStrategy: Int = prepareStrategy
    private val mJsCache: JsLruCache

    init {
        mJsCache = JsLruCache(maxSize)
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
                consumer.invoke(null)
            } else {
                if (mJsCacheStrategy != JsCacheStrategy.NO_CACHE) {
                    mJsCache.put(page, it)
                }
                consumer.invoke(it)
            }
        }
        log("开始加载 ${page.pageName} 缓存大小 => " + mJsCache.size())
        var runnable: (() -> String?)? = null
        when (mJsLoadStrategy) {
        // 只加载网络
            JsLoadStrategy.NET_FIRST -> {
                runnable = {
                    var template: String? = null
                    if (!TextUtils.isEmpty(page.remoteJs)) {
                        log("从 网络 中取得 => ${page.pageName}")
                        template = downloadJs(page)
                    }
                    template
                }
            }
        // 只加载 assets
            JsLoadStrategy.ASSETS_FIRST -> {
                runnable = {
                    var template: String? = null
                    if (!TextUtils.isEmpty(page.assetsJs)) {
                        log("从 assets 中取得 => ${page.pageName}")
                        template = WXFileUtils.loadAsset(page.assetsJs, context)
                    }
                    template
                }
            }
        // 只加载文件
            JsLoadStrategy.FILE_FIRST -> {
                runnable = {
                    var template: String? = null
                    if (!TextUtils.isEmpty(page.localJs) && !FileUtils.isNotExist(page.localJs)) {
                        log("从文件中取得 => ${page.pageName}")
                        template = WXFileUtils.loadFileOrAsset(page.localJs, context)
                    }
                    template
                }
            }
        // 只加载缓存
            JsLoadStrategy.CACHE_FIRST -> {
                runnable = {
                    mJsCache.get(page)
                }
            }
        // 默认 缓存 -> 文件 -> assets -> 网络
            JsLoadStrategy.DEFAULT -> {
                runnable = {
                    var template: String? = mJsCache.get(page)
                    if (!TextUtils.isEmpty(template)) {
                        log("从缓存中取得 => ${page.pageName}")
                        consumer.invoke(template)
                    } else if (!TextUtils.isEmpty(page.localJs) && !FileUtils.isNotExist(page.localJs)) {
                        log("从文件中取得 => ${page.pageName}")
                        template = WXFileUtils.loadFileOrAsset(page.localJs, context)
                    } else if (!TextUtils.isEmpty(page.assetsJs)) {
                        log("从 assets 中取得 => ${page.pageName}")
                        template = WXFileUtils.loadAsset(page.assetsJs, context)
                    } else if (!TextUtils.isEmpty(page.remoteJs)) {
                        log("从 网络 中取得 => ${page.pageName}")
                        template = downloadJs(page)
                    }
                    template
                }
            }
        }
        mService.execute {
            val template = runnable?.invoke()
            publishFunc(template)
        }
    }

    fun clear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mJsCache.trimToSize(-1)
        }
    }

    private fun downloadJs(page: WeexPage): String? {
        val url = page.remoteJs ?: return null
        val http = ManagerRegistry.HTTP
        val wxRequest = http.makeWxRequest(url = url, from = "download-js")
        val resp = http.requestSync(wxRequest, false)
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

        fun log(msg: String) {
            Weex.getInst().mWeexInjector.onLog(TAG, msg)
        }

    }

    class JsLruCache(maxNum: Int) : LruCache<WeexPage, String>(maxNum) {
        override fun sizeOf(key: WeexPage, value: String): Int {
            return value.length
        }
    }


}
