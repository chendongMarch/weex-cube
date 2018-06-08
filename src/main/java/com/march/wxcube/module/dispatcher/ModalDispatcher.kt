package com.march.wxcube.module.dispatcher

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.alibaba.fastjson.JSONObject
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.mWeexAct
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
        const val loading = "loading"
        const val alert = "alert"
        const val confirm = "confirm"
        const val prompt = "prompt"
    }

    override fun getMethods(): List<String> {
        return listOf(toast, loading)
    }

    override fun dispatch(method: String, params: JSONObject, callback: JSCallback) {
        when (method) {
            toast   -> toast(params, callback)
            loading -> loading(params, callback)
        }
    }

    private fun toast(params: JSONObject, callback: JSCallback) {
        val duration = params.getDef(KEY_DURATION, 2)
        val msg = params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.show(msg)
        } else {
            ToastUtils.showLong(msg)
        }
        mModule.postJsResult(callback, true to "Modal#toast finishWeexLoading")
    }

    private fun loading(params: JSONObject, callback: JSCallback) {
        val msg = params.getDef(KEY_MSG, "")
        val show = params.getDef("show", false)
        val weexAct = mModule.mWeexAct ?: throw RuntimeException("ModuleDispatcher#mWeexAct error")
        val parentView = weexAct.mDelegate.mContainerView
        val progressView = weexAct.mProgressBar
        val notLoading = parentView.indexOfChild(progressView) == -1
        if (show && notLoading) {
            val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            parentView.addView(progressView, layoutParams)
            progressView.visibility = View.VISIBLE
        } else if (!show && !notLoading){
            progressView.visibility = View.GONE
            parentView.removeView(progressView)
        }
        mModule.postJsResult(callback, true to "Modal#loading finishWeexLoading")
    }

}