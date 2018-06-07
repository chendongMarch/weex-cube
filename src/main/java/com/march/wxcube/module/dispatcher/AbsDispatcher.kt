package com.march.wxcube.module.dispatcher

import android.support.v7.app.AppCompatActivity
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.OneModule
import com.march.wxcube.module.mAct
import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/6
 * Describe :
 *
 * @author chendong
 */
abstract class AbsDispatcher {

    lateinit var mModule: OneModule

    companion object {
        // key
        const val KEY_SUCCESS = "success"
        const val KEY_MSG = "msg"
        const val KEY_URL = "url"
        const val KEY_TAG = "tag"
        const val KEY_EVENT = "event"
        const val KEY_DATA = "data"
        const val KEY_DURATION = "duration"

    }

    abstract fun getMethods(): List<String>

    abstract fun dispatch(method: String, params: JSONObject, callback: JSCallback)

    fun findAct(): AppCompatActivity {
        return mModule.mAct ?: throw RuntimeException("ModuleDispatcher#act error")
    }
}