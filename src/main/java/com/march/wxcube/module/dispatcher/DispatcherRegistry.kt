package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/9
 * Describe : 统一管理注册进来的 dispatcher
 *
 * @author chendong
 */
class DispatcherRegistry(provider: BaseDispatcher.Provider, vararg dispatchers: BaseDispatcher) : BaseDispatcher() {

    override fun getMethods(): Array<String> {
        return arrayOf()
    }

    private val mMethodDispatcher by lazy { mutableMapOf<String, BaseDispatcher>() }
    private val mAsyncMethods by lazy { mutableListOf<String>() }
    init {
        for (dispatcher in dispatchers) {
            registerDispatcher(dispatcher, provider)
        }
    }

    private fun registerDispatcher(dispatcher: BaseDispatcher, provider: BaseDispatcher.Provider) {
        mAsyncMethods.addAll(dispatcher.getAsyncMethods())
        for (method in dispatcher.getMethods()) {
            dispatcher.mProvider = provider
            mMethodDispatcher[method] = dispatcher
        }
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val dispatcher = mMethodDispatcher[method] ?: throw RuntimeException("method $method not match")
        dispatcher.dispatch(method, params,jsCallbackWrap)
        // 不是异步方法直接返回结束
        if (!mAsyncMethods.contains(method)) {
            postJsResult(jsCallbackWrap, true to "$method($params) finish ")
        }
    }
}