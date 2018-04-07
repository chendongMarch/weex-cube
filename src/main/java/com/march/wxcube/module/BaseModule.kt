package com.march.wxcube.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.march.common.utils.LogUtils
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


    protected fun findView(f: (View) -> Boolean): View? {
        val containerView: ViewGroup = mWXSDKInstance.containerView as ViewGroup
        return findView(containerView, f)
    }

    protected tailrec fun findView(viewGroup: ViewGroup, f: (View) -> Boolean): View? {
        var view: View
        for (i in (0 until viewGroup.childCount)) {
            view = viewGroup.getChildAt(i)
            LogUtils.e(view.toString() + "  " + view.tag)
            val result = f.invoke(view)
            if (result) {
                return view
            } else if (view is ViewGroup) {
                return findView(view, f)
            }
        }
        return null
    }

    protected fun <T> jsonObj2Obj(objectMap: JSONObject, clz: Class<T>): T {
        val json = JSON.toJSONString(objectMap)
        return JSONObject.parseObject(json, clz)
    }


    protected fun <T> jsonArray2List(jsonArray: JSONArray, clz: Class<T>): List<T> {
        val json = JSON.toJSONString(jsonArray)
        return JSONArray.parseArray(json, clz)
    }
}
