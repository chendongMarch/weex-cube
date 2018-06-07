package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.common.utils.LogUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/6
 * Describe : 调试模块分发
 *
 * @author chendong
 */
class DebugDispatcher : AbsDispatcher() {

    companion object {

        const val debugToast = "debugToast"
        const val debugLog = "debugL"
    }

    override fun getMethods(): List<String> {
        return listOf(debugToast, debugLog)
    }

    override fun dispatch(method: String, params: JSONObject, callback: JSCallback) {
        when (method) {
            debugToast -> toast(params, callback)
            debugLog   -> log(params, callback)
        }
    }

    private fun toast(params: JSONObject, callback: JSCallback) {
        val duration = params.getDef(KEY_DURATION, 2)
        val msg = params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.showLong(msg)
        } else {
            ToastUtils.show(msg)
        }
        mModule.postJsResult(callback, true to "toast msg")
    }

    private fun log(params: JSONObject, callback: JSCallback) {
        val tag = params.getDef(KEY_TAG, "weex-debug")
        val msg = params.getDef(KEY_MSG, "no msg")
        LogUtils.e(tag, msg)
        mModule.postJsResult(callback, true to "log msg")
    }
}