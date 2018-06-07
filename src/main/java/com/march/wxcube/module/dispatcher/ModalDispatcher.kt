package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/6
 * Describe :
 *
 * @author chendong
 */
class ModalDispatcher : AbsDispatcher() {

    companion object {
        const val toast = "toast"
        const val alert = "alert"
        const val confirm = "confirm"
        const val prompt = "prompt"
    }

    override fun getMethods(): List<String> {
        return listOf(toast)
    }

    override fun dispatch(method: String, params: JSONObject, callback: JSCallback) {
        when (method) {
            toast -> toast(params, callback)
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

}