package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject

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

    init {
        for (dispatcher in dispatchers) {
            registerDispatcher(RouterDispatcher(), provider)
        }
    }

    private fun registerDispatcher(dispatcher: BaseDispatcher, provider: BaseDispatcher.Provider) {
        for (method in dispatcher.getMethods()) {
            dispatcher.mProvider = provider
            mMethodDispatcher[method] = dispatcher
        }
    }

    override fun dispatch(method: String, params: JSONObject) {
        val dispatcher = mMethodDispatcher[method] ?: throw RuntimeException("method $method not match")
        dispatcher.dispatch(method, params)
    }
}