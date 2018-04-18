package com.march.wxcube.module

import com.march.wxcube.Weex
import com.taobao.weex.WXSDKManager
import com.taobao.weex.annotation.JSMethod

/**
 * CreateAt : 2018/4/18
 * Describe : 事件
 *
 * @author chendong
 */
class EventModule : BaseModule() {

    companion object {
        // key -> List<instantId>
        val eventInstantIdMap = mutableMapOf<String, MutableList<String>>()
    }

    // 注册接受某事件
    // weex
    // event.registerEvent('myEvent')
    // globalEvent.addEventListener('myEvent', (params) => {});
    @JSMethod
    fun registerEvent(key: String, instantId: String) {
        val registerInstantIds = eventInstantIdMap[key] ?: mutableListOf()
        registerInstantIds.add(instantId)
        eventInstantIdMap[key] = registerInstantIds
    }

    // 发送事件
    // weex
    // event.post('myEvent',{isOk:true});
    @JSMethod
    fun post(key: String, params: Map<String, Any>) {
        if (WXSDKManager.getInstance() == null) {
            Weex.getInst().weexService.onErrorReport(null, "post event WXSDKManager.getInstance() == null")
            return
        }
        val renderManager = WXSDKManager.getInstance().wxRenderManager
        if (renderManager == null) {
            Weex.getInst().weexService.onErrorReport(null, "post event WXSDKManager.getInstance().wxRenderManager == null")
            return
        }
        val registerInstantIds = eventInstantIdMap[key] ?: listOf<String>()
        val allInstants = renderManager.allInstances
        for (instance in allInstants) {
            // 该事件被该 instant 注册过
            if (instance != null
                    && !instance.instanceId.isNullOrEmpty()
                    && registerInstantIds.contains(instance.instanceId)) {
                instance.fireGlobalEventCallback(key, params)
            }
        }
    }
}