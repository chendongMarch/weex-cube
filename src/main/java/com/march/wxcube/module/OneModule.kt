package com.march.wxcube.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import com.march.wxcube.model.DialogConfig
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.bridge.JSCallback
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/6/5
 * Describe :
 *
 * @author chendong
 */
class OneModule : WXModule() {

    companion object {
        // key
        const val KEY_SUCCESS = "success"
        const val KEY_MSG = "msg"
        const val KEY_URL = "url"
        // method
        const val openUrl = "openUrl"
        const val openWeb = "openWeb"
        const val openDialog = "openDialog"
        const val openBrowser = "openBrowser"
    }

    /**
     * 优点：
     * 1. 避免版本不同造成的方法不兼容
     * 2. 方法参数回调统一规范性更好
     * 3. 方法调用失败时，可以从 msg 快速定位问题
     * 缺点：
     * 1. 写起来比较繁琐
     * 2. 调用起来不好识别，最好能有 vue 层的支持
     *
     * const one = weex.requireModule('one-module');
     *
     * Vue.callNative = (method,params,callback) => {
     *     const p = params || {};
     *     const cb = callback || ()=>{};
     *     one.call(method,params,callback);
     *     if(isWeb) {
     *
     *     }
     * }
     *
     * Vue.openUrl = (params,callback) => {
     *     const p = params || {};
     *     const cb = callback || ()=>{};
     *     one.call('openUrl',params,callback);
     * }
     *
     * native.openUrl({},()=>{});
     * native.openUrl();
     * one.call('openUrl',{},()=>{});
     *
     * OneModule$call()
     * #method 调用方法的唯一标识
     * #params 传递的参数
     * #callback 结果回调，必须回调回去
     *      success: boolean
     *      msg: string
     */
    @JSMethod(uiThread = true)
    fun call(method: String, params: JSONObject, callback: JSCallback) {
        try {
            val ctx = mCtx ?: throw RuntimeException("ctx is null")
            val act = mAct ?: throw RuntimeException("act is null")
            when (method) {
                openUrl -> openUrl(ctx, params, callback)
                openWeb -> openWeb(ctx, params, callback)
                openDialog -> openDialog(act, params, callback)
                openBrowser -> openBrowser(ctx, params, callback)
                else -> throw RuntimeException("method not match")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(mapOf(
                    KEY_SUCCESS to false,
                    KEY_MSG to e.message))
        }
    }

    private fun openWeb(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("webUrl is null")
        postJsResult(callback, Weex.getInst().mWeexRouter.openWeb(ctx, webUrl))
    }

    private fun openDialog(act: AppCompatActivity, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("webUrl is null")
        val config = jsonObj2Obj(params, DialogConfig::class.java)
        postJsResult(callback, Weex.getInst().mWeexRouter.openDialog(act, webUrl, config))
    }

    private fun openBrowser(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("webUrl is null")
        postJsResult(callback, Weex.getInst().mWeexRouter.openBrowser(ctx, webUrl))
    }

    private fun openUrl(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("webUrl is null")
        postJsResult(callback, Weex.getInst().mWeexRouter.openUrl(ctx, webUrl))
    }

    private fun postJsResult(jsCallback: JSCallback, result: Pair<Boolean, String>) {
        jsCallback.invoke(mapOf(
                KEY_SUCCESS to result.first,
                KEY_MSG to result.second))
    }
}