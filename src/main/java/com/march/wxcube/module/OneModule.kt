package com.march.wxcube.module

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.dispatcher.*
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.bridge.JSCallback
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/6/5
 * Describe : 单 module 实现
 *
 * @author chendong
 */
class OneModule : WXModule() {

    private val mMethodDispatcher by lazy { mutableMapOf<String, AbsDispatcher>() }

    init {
        registerDispatcher(RouterDispatcher())
        registerDispatcher(DebugDispatcher())
        registerDispatcher(ModalDispatcher())
        registerDispatcher(PageDispatcher())
        registerDispatcher(EventDispatcher())
        registerDispatcher(ToolsDispatcher())
        registerDispatcher(StatusBarDispatcher())
    }

    /**
     * 注册方法的处理者
     */
    private fun registerDispatcher(dispatcher: AbsDispatcher) {
        for (method in dispatcher.getMethods()) {
            dispatcher.mModule = this
            mMethodDispatcher[method] = dispatcher
        }
    }


    /**
     * 优点：
     * 1. 避免版本不同造成的方法不兼容
     * 2. 更好的兼容客户端和H5，不会因为某个方法没有造成渲染失败
     * 3. 方法参数回调统一规范性更好
     * 4. 方法调用失败时，可以从 msg 快速定位问题
     * 5. 更好的扩展性，由于统一了参数，扩展 params 也变得简单啦
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
            val dispatcher = mMethodDispatcher[method] ?: throw RuntimeException("method not match")
            dispatcher.dispatch(method, params, callback)
        } catch (e: Exception) {
            e.printStackTrace()
            postJsResult(callback, false to (e.message ?: ""))
        }
    }

    fun postJsResult(jsCallback: JSCallback, result: Pair<Boolean, String>) {
        jsCallback.invoke(mapOf(
                AbsDispatcher.KEY_SUCCESS to result.first,
                AbsDispatcher.KEY_MSG to result.second))
    }
}