package com.march.wxcube

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.LruCache
import com.march.common.utils.FileUtils
import com.march.wxcube.manager.ManagerRegistry

import com.march.wxcube.model.WeexPage
import com.taobao.weex.common.WXRequest
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
class WeexJsLoader(loadStrategy: Int, maxSize: Int) {

    private val mService: ExecutorService = Executors.newFixedThreadPool(1)
    private val mJsLoadStrategy: Int = loadStrategy
    private val mJsCache: JsLruCache

    init {
        mJsCache = JsLruCache(maxSize)
    }

    fun update(context: Context, weexPages: List<WeexPage>) {
        if (mJsLoadStrategy == JsLoadStrategy.PREPARE_ALL) {
            for (page in weexPages) {
                getTemplateAsync(context, page) {}
            }
        }
    }

    fun getTemplateAsync(context: Context, page: WeexPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        log("开始加载 ${page.pageName} 缓存大小 => " + mJsCache.size())
        if (mJsLoadStrategy != JsLoadStrategy.ALWAYS_FRESH) {
            val template = mJsCache.get(page)
            if (!TextUtils.isEmpty(template)) {
                log("从缓存中取得 => ${page.pageName}")
                consumer.invoke(template)
                return
            }
        }
        mService.execute {
            var template: String? = null
            if (!TextUtils.isEmpty(page.localJs) && !FileUtils.isNotExist(page.localJs)) {
                log("从文件中取得 => ${page.pageName}")
                template = WXFileUtils.loadFileOrAsset(page.localJs, context)
            } else if (!TextUtils.isEmpty(page.assetsJs)) {
                log("从 assets 中取得 => ${page.pageName}")
                template = WXFileUtils.loadAsset(page.assetsJs, context)
            } else if (!TextUtils.isEmpty(page.remoteJs)) {
                log("从 网络 中取得 => ${page.pageName}")
                template = downloadJs(page)
            }
            if (!TextUtils.isEmpty(template)) {
                mJsCache.put(page, template)
                consumer.invoke(template)
            } else {
                consumer.invoke(null)
            }
        }
    }

    fun clear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mJsCache.trimToSize(-1)
        }
    }

    private fun downloadJs(page: WeexPage): String? {
        val wxRequest = WXRequest()
        wxRequest.url = page.remoteJs
        wxRequest.method = "get"
        wxRequest.paramMap = mapOf("from" to "download-js")
        val resp = ManagerRegistry.HTTP.requestSync(wxRequest)
        val originalData = resp.originalData
        return if (resp.originalData == null) null
        else String(originalData, Charset.forName("utf-8"))
    }

    object JsLoadStrategy {
        const val ALWAYS_FRESH = 0 // 总是使用最新的
        const val PREPARE_ALL = 1 // 提前准备
        const val LAZY_LOAD = 2 // 使用时才加载，并缓存
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
