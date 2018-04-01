package com.march.wxcube.module

import android.util.Log
import android.widget.Toast

import com.taobao.weex.annotation.JSMethod

/**
 * CreateAt : 2018/3/29
 * Describe :
 *
 * @author chendong
 */
class DebugModule : BaseModule() {

    @JSMethod
    fun log(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    @JSMethod
    fun logMsg(msg: String) {
        Log.e(TAG, msg)
    }

    @JSMethod
    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    @JSMethod
    fun toastLong(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }

    companion object {

        val TAG = DebugModule::class.java.simpleName!!
    }
}
