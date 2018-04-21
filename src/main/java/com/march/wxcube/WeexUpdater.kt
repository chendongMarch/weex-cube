package com.march.wxcube

import android.content.Context
import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.march.wxcube.common.report
import com.march.wxcube.common.toSafeUrl
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
class WeexUpdater {

    class WeexPagesResp() {
        var total: Int? = 0
        var datas: List<WeexPage>? = null
    }

    /**
     * 更新数据源
     */
    fun updateWeexPages(context: Context, weexPages: List<WeexPage>?) {
        val pages = weexPages ?: return
        val list = pages.filterNot { TextUtils.isEmpty(it.webUrl) }
        Weex.getInst().mWeexRouter.update(list)
        Weex.getInst().mWeexJsLoader.update(context, list)
    }

    fun updateWeexPages(context: Context, url: String) {
        val http = ManagerRegistry.HTTP
        val request = http.makeWxRequest(url = url, from = "request-config")
        http.request(request, object : HttpListener {
            override fun onHttpFinish(response: WXResponse) {
                if (response.errorCode == HttpManager.ERROR_CODE) {
                    report("请求配置文件失败")
                } else {
                    val json = response.data ?: return
                    try {
                        val weexPagesResp = JSON.parseObject(json, WeexPagesResp::class.java)
                        weexPagesResp?.datas?.forEach {
                            it.webUrl = it.webUrl?.toSafeUrl()
                        }
                        updateWeexPages(context, weexPagesResp?.datas)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, false)
    }
}
