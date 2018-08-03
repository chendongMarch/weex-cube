package com.march.wxcube.module.dispatcher

import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.BridgeModule
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs
import com.march.wxcube.module.mInstId

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class EventDispatcher(val module: BridgeModule) : BaseDispatcher() {

    /**
     * 注册接受某事件
     * const event = weex.requireModule('cube-event')
     * event.registerEvent('myEvent')
     */
    @DispatcherJsMethod
    fun registerEvent(args: WxArgs) {
        val instanceId = module.mInstId ?: throw RuntimeException("Event#registerEvent instanceId is null ${args.params.toJSONString()}")
        val event = args.params.getString(KEY_EVENT) ?: throw RuntimeException("Event#registerEvent event is null ${args.params.toJSONString()}")
        ManagerRegistry.Event.registerEvent(event, instanceId)
    }


    /**
     * 发送事件
     * const event = weex.requireModule('cube-event')
     * event.postEvent('myEvent',{isOk:true});
     */
    @DispatcherJsMethod
    fun postEvent(args: WxArgs) {
        val event = args.params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${args.params.toJSONString()}")
        val data = args.params.getJSONObject(KEY_DATA) ?: throw RuntimeException("Event#postEvent event is null ${args.params.toJSONString()}")
        ManagerRegistry.Event.postEvent(event, data.toMap())
    }


    /**
     * 取消注册事件
     * const event = weex.requireModule('cube-event')
     * event.unRegisterEvent('myEvent');
     */
    @DispatcherJsMethod
    fun unRegisterEvent(args: WxArgs) {
        val instanceId = module.mInstId ?: throw RuntimeException("Event#unRegisterEvent instanceId is null ${args.params.toJSONString()}")
        val event = args.params.getString(KEY_EVENT) ?: throw RuntimeException("Event#postEvent event is null ${args.params.toJSONString()}")
        ManagerRegistry.Event.unRegisterEvent(event, instanceId)
    }
}