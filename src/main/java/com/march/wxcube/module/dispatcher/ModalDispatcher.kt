package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/6
 * Describe :
 *
 * @author chendong
 */
class ModalDispatcher : BaseDispatcher() {

    companion object {
        const val toast = "toast"
        const val loading = "loading"
        const val alert = "alert"
        const val confirm = "confirm"
        const val prompt = "prompt"
    }

    override fun getMethods(): List<String> {
        return listOf(toast, loading)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        when (method) {
            toast   -> toast(params)
            loading -> loading(params)
        }
    }

    private fun toast(params: JSONObject) {
        val duration = params.getDef(KEY_DURATION, 2)
        val msg = params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.show(msg)
        } else {
            ToastUtils.showLong(msg)
        }
    }

    private fun loading(params: JSONObject) {
        mProvider.doBySelf(loading, params)
    }

}