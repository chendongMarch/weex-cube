package com.march.wxcube

import android.content.Context
import android.text.TextUtils
import android.util.LruCache
import com.march.common.utils.FileUtils
import com.march.common.utils.StreamUtils

import com.march.wxcube.model.WeexPage
import com.taobao.weex.utils.WXFileUtils

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/3/27
 * Describe : 对 page 对应的 template 做缓存，加速访问，防止频繁 IO
 *
 * @author chendong
 */
class WeexBundleCache : LruCache<WeexPage, String>(10 * 1024 * 1024) {

    private val mService: ExecutorService = Executors.newFixedThreadPool(1)

    fun getTemplateAsync(context: Context, page: WeexPage?, consumer: (String?) -> Unit) {
        if (page == null) {
            return
        }
        Weex.instance.weexService.onLog(TAG, "缓存大小 => " + size())
        if (Weex.getInst().jsLoadStrategy != Weex.JsLoadStrategy.ALWAYS_FRESH) {
            val template = get(page)
            if (!TextUtils.isEmpty(template)) {
                Weex.instance.weexService.onLog(TAG, "从缓存中取得 => ${page.pageName}")
                consumer.invoke(template)
                return
            }
        }
        mService.execute {
            var template: String? = null
            if (!TextUtils.isEmpty(page.localJs) && !FileUtils.isNotExist(page.localJs)) {
                Weex.instance.weexService.onLog(TAG, "从文件中取得 => ${page.pageName}")
                template = WXFileUtils.loadFileOrAsset(page.localJs, context)
            } else if (!TextUtils.isEmpty(page.assetsJs)) {
                Weex.instance.weexService.onLog(TAG, "从 assets 中取得 => ${page.pageName}")
                template = WXFileUtils.loadAsset(page.assetsJs, context)
            } else if (!TextUtils.isEmpty(page.remoteJs)) {
                Weex.instance.weexService.onLog(TAG, "从 网络 中取得 => ${page.pageName}")
                template = downloadJs(page)
            }
            // 返回数据
            if (!TextUtils.isEmpty(template)) {
                put(page, template)
                consumer.invoke(template)
            } else {
                consumer.invoke(null)
            }
        }
    }

    override fun sizeOf(key: WeexPage, value: String): Int {
        return value.length
    }

    private fun downloadJs(page: WeexPage): String? {
        return try {
            val connection = URL(page.remoteJs).openConnection() as HttpURLConnection
            val inputStream = StreamUtils.openHttpStream(connection)
            StreamUtils.saveStreamToString(inputStream)
        } catch (e: IOException) {
            Weex.instance.weexService.onErrorReport(e, "downloadJs Error")
            null
        }
    }

    companion object {
        val TAG = WeexBundleCache::class.java.simpleName!!
    }

}
