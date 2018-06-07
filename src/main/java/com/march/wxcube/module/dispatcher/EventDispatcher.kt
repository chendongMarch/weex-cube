package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.mInstId
import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class EventDispatcher : AbsDispatcher() {

    companion object {
        const val registerEvent = "registerEvent"
        const val postEvent = "postEvent"
        const val unRegisterEvent = "unRegisterEvent"
    }

    override fun dispatch(method: String, params: JSONObject, callback: JSCallback) {
        when (method) {
            registerEvent   -> registerEvent(params, callback)
            postEvent       -> postEvent(params, callback)
            unRegisterEvent -> unRegisterEvent(params, callback)
        }
    }

    override fun getMethods(): List<String> {
        return listOf(registerEvent, postEvent, unRegisterEvent)
    }

    /**
     * 注册接受某事件
     * const event = weex.requireModule('cube-event')
     * event.registerEvent('myEvent')
     */
    private fun registerEvent(params: JSONObject, callback: JSCallback) {
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#registerEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.registerEvent(event, mModule.mInstId)
        mModule.postJsResult(callback, true to "Event#registerEvent success")
    }


    /**
     * 发送事件
     * const event = weex.requireModule('cube-event')
     * event.postEvent('myEvent',{isOk:true});
     */
    private fun postEvent(params: JSONObject, callback: JSCallback) {
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        val data = params.getJSONObject(KEY_DATA) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.postEvent(event, data.toMap())
        mModule.postJsResult(callback, true to "Event#postEvent success")
    }


    /**
     * 取消注册事件
     * const event = weex.requireModule('cube-event')
     * event.unRegisterEvent('myEvent');
     */
    private fun unRegisterEvent(params: JSONObject, callback: JSCallback) {
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.unRegisterEvent(event, mModule.mInstId)
        mModule.postJsResult(callback, true to "Event#unRegisterEvent success")
    }
}