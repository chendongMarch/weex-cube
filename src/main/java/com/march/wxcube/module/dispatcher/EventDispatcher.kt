package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.JsCallbackWrap
import com.march.wxcube.module.OneModule
import com.march.wxcube.module.mInstId

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class EventDispatcher(val module: OneModule) : BaseDispatcher() {

    companion object {
        const val registerEvent = "registerEvent"
        const val postEvent = "postEvent"
        const val unRegisterEvent = "unRegisterEvent"
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        when (method) {
            registerEvent   -> registerEvent(params)
            postEvent       -> postEvent(params)
            unRegisterEvent -> unRegisterEvent(params)
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
    private fun registerEvent(params: JSONObject) {
        val instanceId = module.mInstId ?: throw RuntimeException("Event#registerEvent instanceId is null ${params.toJSONString()}")
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#registerEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.registerEvent(event, instanceId)
    }


    /**
     * 发送事件
     * const event = weex.requireModule('cube-event')
     * event.postEvent('myEvent',{isOk:true});
     */
    private fun postEvent(params: JSONObject) {
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        val data = params.getJSONObject(KEY_DATA) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.postEvent(event, data.toMap())
    }


    /**
     * 取消注册事件
     * const event = weex.requireModule('cube-event')
     * event.unRegisterEvent('myEvent');
     */
    private fun unRegisterEvent(params: JSONObject) {
        val instanceId = module.mInstId ?: throw RuntimeException("Event#unRegisterEvent instanceId is null ${params.toJSONString()}")
        val event = params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${params.toJSONString()}")
        ManagerRegistry.EVENT.unRegisterEvent(event, instanceId)
    }
}