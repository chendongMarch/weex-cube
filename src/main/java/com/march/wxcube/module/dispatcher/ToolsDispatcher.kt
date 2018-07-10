package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/6
 * Describe : 工具模块分发
 *
 * @author chendong
 */
class ToolsDispatcher : BaseDispatcher() {

    companion object {
        const val clearCookies = "clearCookies"
    }
    override fun getMethods(): Array<String> {
        return arrayOf(clearCookies)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        when(method) {
            clearCookies -> clearCookies()
        }
    }

    private fun clearCookies() {
        ManagerRegistry.Request.getCookieJar().clear()
    }

}