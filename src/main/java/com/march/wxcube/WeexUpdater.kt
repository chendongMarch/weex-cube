package com.march.wxcube

import android.content.Context
import android.text.TextUtils
import com.alibaba.fastjson.JSON
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
    fun updateWeexPages(postIndex: Boolean, context: Context, pages: List<WeexPage>?)
}

class WeexUpdater(private val url: String) : UpdateHandler {


    companion object {
        private const val CONFIG_CACHE_KEY = "CONFIG_CACHE_KEY"
    }

    class WeexPagesResp {
        var total: Int? = 0
        var datas: List<WeexPage>? = null
    }

    override fun updateWeexPages(postIndex: Boolean, context: Context, weexPages: List<WeexPage>?) {
        val pages = weexPages ?: return
        pages.filterNot { it.webUrl.isNullOrBlank() }
                .forEach { it.webUrl = ManagerRegistry.ENV.checkAddHost(it.webUrl) }
        Weex.getInst().mWeexRouter.updateWeexPages(postIndex, context, pages)
        Weex.getInst().mWeexJsLoader.updateWeexPages(postIndex, context, pages)
    }


    fun requestPages(postIndex: Boolean, context: Context) {
        val http = ManagerRegistry.HTTP
        val listener: HttpListener = object : HttpListener {
            override fun onHttpFinish(response: WXResponse) {
                if (response.errorCode == HttpManager.ERROR_CODE) {
                    report("请求配置文件失败")
                } else {
                    val json = response.data ?: return
                    try {
                        val weexPagesResp = JSON.parseObject(json, WeexPagesResp::class.java)
                        updateWeexPages(postIndex, context, weexPagesResp?.datas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        when {
            url.isBlank() -> {
            }
            url.startsWith("file") -> http.requestFile(url.replace("file://", ""), listener)
            url.startsWith("assets") -> http.requestAssets(context, url.replace("assets://", ""), listener)
            else -> {
                val request = http.makeWxRequest(url = url, from = "request-config")
                http.request(request, listener, false)
            }
        }
    }
}
