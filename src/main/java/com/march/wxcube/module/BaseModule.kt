package com.march.wxcube.module

import android.content.Context
import android.support.v7.app.AppCompatActivity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.ui.WeexActivity
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/3/28
 * Describe : Module
 *
 * @author chendong
 */
open class BaseModule : WXModule() {

    protected val activity: AppCompatActivity?
        get() {
            if (mWXSDKInstance != null) {
                val context = mWXSDKInstance.context
                if (context is AppCompatActivity) {
                    return context
                }
            }
            return null
        }

    protected val weexActivity: WeexActivity?
        get() {
            if (mWXSDKInstance != null) {
                val context = mWXSDKInstance.context
                if (context is WeexActivity) {
                    return context
                }
            }
            return null
        }

    protected val context: Context?
        get() = if (mWXSDKInstance != null) {
            mWXSDKInstance.context
        } else null


    protected fun <T> map2Obj(objectMap: Map<String, Any>, clz: Class<T>): T {
        val json = JSON.toJSONString(objectMap)
        return JSONObject.parseObject(json, clz)
    }


    protected fun <T> jsonArray2List(jsonArray: JSONArray, clz: Class<T>): List<T> {
        val json = JSON.toJSONString(jsonArray)
        return JSONArray.parseArray(json, clz)
    }
}
