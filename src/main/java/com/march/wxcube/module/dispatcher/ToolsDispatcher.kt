package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/6
 * Describe : 调试模块分发
 *
 * @author chendong
 */
class ToolsDispatcher : BaseDispatcher() {
    override fun getMethods(): Array<String> {
        return arrayOf()
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
    }

}