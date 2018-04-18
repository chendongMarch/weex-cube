package com.march.wxcube.hub

import com.march.wxcube.Weex
import com.march.wxcube.manager.BaseManager
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.WXSDKManager


/**
 * CreateAt : 2018/4/18
 * Describe : 事件中转
 *
 * @author chendong
 */
object EventHub  {

    // key -> List<instantId>
    private val eventInstantIdMap = mutableMapOf<String, MutableSet<String>>()

    fun clear(instance: WXSDKInstance) {
        val nonNullId = instance.instanceId ?: return
        for (mutableEntry in eventInstantIdMap) {
            if (mutableEntry.value.isNotEmpty()) {
                mutableEntry.value.remove(nonNullId)
            }
        }
    }

    // 注册接受某事件
    // weex
    // event.registerEvent('myEvent')
    // globalEvent.addEventListener('myEvent', (params) => {});
    fun registerEvent(key: String?, instantId: String?) {
        if (instantId == null) {
            Weex.getInst().weexService.onErrorReport(null, "registerEvent error instantId = null")
            return
        }
        val nonNullKey = key ?: return
        val nonNullId = instantId ?: return
        val registerInstantIds = eventInstantIdMap[nonNullKey] ?: mutableSetOf()
        registerInstantIds.add(nonNullId)
        eventInstantIdMap[nonNullKey] = registerInstantIds
    }

    // 发送事件
    // weex
    // event.post('myEvent',{isOk:true});
    fun postEvent(key: String, params: Map<String, Any>) {
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