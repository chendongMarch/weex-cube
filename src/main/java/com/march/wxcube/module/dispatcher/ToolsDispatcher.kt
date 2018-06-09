package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject

/**
 * CreateAt : 2018/6/6
 * Describe : 调试模块分发
 *
 * @author chendong
 */
class ToolsDispatcher : BaseDispatcher() {
    override fun getMethods(): List<String> {
        return listOf()
    }

    override fun dispatch(method: String, params: JSONObject) {
    }

}