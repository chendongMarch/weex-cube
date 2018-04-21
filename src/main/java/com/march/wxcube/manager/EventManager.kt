package com.march.wxcube.manager

import com.march.wxcube.common.report
import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.WXSDKManager


/**
 * CreateAt : 2018/4/18
 * Describe : 事件中转
 *
 * @author chendong
 */
class EventManager : IManager {

    companion object {
        val instance: EventManager by lazy { EventManager() }
    }

    // key -> List<instantId>
    private val eventInstantIdMap = mutableMapOf<String, MutableSet<String>>()

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
        val nonNullId = instance?.instanceId ?: return
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
            report("registerEvent error instantId = null")
            return
        }
        val nonNullKey = key ?: return
        val registerInstantIds = eventInstantIdMap[nonNullKey] ?: mutableSetOf()
        registerInstantIds.add(instantId)
        eventInstantIdMap[nonNullKey] = registerInstantIds
    }

    // 发送事件
    // weex
    // event.post('myEvent',{isOk:true});
    fun postEvent(key: String, params: Map<String, Any>) {
        if (WXSDKManager.getInstance() == null) {
            report("post event WXSDKManager.getInstance() == null")
            return
        }
        val renderManager = WXSDKManager.getInstance().wxRenderManager
        if (renderManager == null) {
            report("post event WXSDKManager.getInstance().wxRenderManager == null")
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