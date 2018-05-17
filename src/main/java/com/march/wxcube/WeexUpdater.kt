package com.march.wxcube

import android.content.Context
import com.alibaba.fastjson.JSON
import com.march.common.utils.LogUtils
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.report
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.HttpManager
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.taobao.weex.common.WXResponse
import java.lang.Exception

/**
 * CreateAt : 2018/4/21
 * Describe :
 *
 * @author chendong
 */
interface UpdateHandler {
    fun updateWeexPages(context: Context, weexPages: List<WeexPage>?)
}

class WeexUpdater(private val url: String) : UpdateHandler {

    private val mDiskLruCache by lazy {
        DiskLruCache(Weex.getInst().makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }

    companion object {
        private const val CONFIG_KEY = "weex-config"
        private const val CACHE_DIR = "config-cache"
        private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    }

    class WeexPagesResp {
        var total: Int? = 0
        var datas: List<WeexPage>? = null
    }

    override fun updateWeexPages(context: Context, weexPages: List<WeexPage>?) {
        val pages = weexPages ?: return
        pages.filterNot { it.webUrl.isNullOrBlank() }
                .forEach { it.webUrl = ManagerRegistry.ENV.checkAddHost(it.webUrl) }
        Weex.getInst().mWeexRouter.updateWeexPages(context, pages)
        Weex.getInst().mWeexJsLoader.updateWeexPages(context, pages)
    }

    fun parseJsonAndUpdate(context: Context, json: String) {
        try {
            val weexPagesResp = JSON.parseObject(json, WeexPagesResp::class.java)
            updateWeexPages(context, weexPagesResp?.datas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestPages(context: Context) {
        val cacheJson = mDiskLruCache.read(CONFIG_KEY)
        if (cacheJson != null) {
            LogUtils.e("先更新本地数据")
            parseJsonAndUpdate(context, cacheJson)
        }
        val http = ManagerRegistry.HTTP
        val listener: HttpListener = object : HttpListener {
            override fun onHttpFinish(response: WXResponse) {
                if (response.errorCode == HttpManager.ERROR_CODE) {
                    report("请求配置文件失败")
                } else {
                    val json = response.data ?: return
                    mDiskLruCache.write(CONFIG_KEY, json)
                    parseJsonAndUpdate(context, json)
                }
            }
        }
        when {
            url.isBlank() -> {
            }
            url.startsWith("file") -> http.requestFile(url.replace("file://", ""), listener)
            url.startsWith("assets") -> http.requestAssets(context, url.replace("assets://", ""), listener)
            else -> {
                val request = http.makeWxRequest(url = url, from = "request-mWeexConfig")
                http.request(request, listener, false)
            }
        }
    }
}
