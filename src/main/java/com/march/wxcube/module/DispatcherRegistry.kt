package com.march.wxcube.module

import com.alibaba.fastjson.JSONObject
import com.march.common.utils.LgUtils
import com.march.wxcube.module.dispatcher.BaseDispatcher

/**
 * CreateAt : 2018/6/9
 * Describe : 统一管理注册进来的 dispatcher
 *
 * @author chendong
 */
class DispatcherRegistry(provider: Provider, vararg dispatchers: BaseDispatcher) : BaseDispatcher() {

    private val mAsyncMethods by lazy { mutableListOf<String>() }

    private val mMethodMap by lazy { mutableMapOf<String, DispatcherMethod>() }

    init {
        for (dispatcher in dispatchers) {
            registerDispatcher(dispatcher, provider)
        }
    }

    private fun registerDispatcher(dispatcher: BaseDispatcher, provider: Provider) {
        dispatcher.mProvider = provider
        initMethods(dispatcher)
        dispatcher.mMethods.forEach {
            mMethodMap[it.name] = it
            if (it.async) {
                mAsyncMethods.add(it.name)
            }
        }
    }

    fun dispatch(method: String, params: JSONObject, jsCallbackWrap: Callback) {
        val dispatcherMethod = mMethodMap[method]?: throw RuntimeException("method $method not match")
        dispatcherMethod.method.invoke(dispatcherMethod.dispatcher, WxArgs(method, params, jsCallbackWrap))
        // 不是异步方法直接返回结束
        if (!mAsyncMethods.contains(method)) {
            postJsResult(jsCallbackWrap, true to "$method($params) finish ")
        }
    }


    private fun initMethods(dispatcher: BaseDispatcher) {
        val declaredMethods = dispatcher::class.java.declaredMethods
        declaredMethods.forEach { jsMethod ->
            val annotation = jsMethod.getAnnotation(DispatcherJsMethod::class.java)
            annotation?.let {
                val method = DispatcherMethod(
                        annotation.UI,
                        annotation.async,
                        if (annotation.alias.isBlank()) jsMethod.name else annotation.alias,
                        jsMethod,
                        dispatcher)
                dispatcher.mMethods.add(method)
            }
        }
    }
}