package com.march.wxcube.module

import android.util.Log
import android.widget.Toast

import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/3/29
 * Describe : 调试
 *
 * @author chendong
 */
class DebugModule : WXModule() {

    companion object {
        val TAG = DebugModule::class.java.simpleName!!
        const val KEY = "cube-debug"
    }

    /**
     * 打印日志带 tag
     */
    @JSMethod(uiThread = true)
    fun log(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    /**
     * 打印日志使用默认 tag
     */
    @JSMethod(uiThread = true)
    fun logMsg(msg: String) {
        Log.e(TAG, msg)
    }

    /**
     * 弹 toast
     */
    @JSMethod(uiThread = true)
    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * 弹长 toast
     */
    @JSMethod(uiThread = true)
    fun toastLong(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}
