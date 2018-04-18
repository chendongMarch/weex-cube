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

    @JSMethod(uiThread = true)
    fun log(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    @JSMethod(uiThread = true)
    fun logMsg(msg: String) {
        Log.e(TAG, msg)
    }

    @JSMethod(uiThread = true)
    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JSMethod(uiThread = true)
    fun toastLong(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    companion object {
        val TAG = DebugModule::class.java.simpleName!!
    }
}
