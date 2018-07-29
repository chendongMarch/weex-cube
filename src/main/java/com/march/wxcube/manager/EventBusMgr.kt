package com.march.wxcube.manager

import com.march.wxcube.common.log
import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.WXSDKManager


/**
 * CreateAt : 2018/4/18
 * Describe : 事件中转
 *
 * @author chendong
 */
class EventBusMgr : IManager {

    companion object {
        val instance: EventBusMgr by lazy { EventBusMgr() }
    }

    // EventKey -> List<mInstId>
    private val mEventInstanceIdMap by lazy { mutableMapOf<String, MutableSet<String>>() }

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {
        val nonNullId = instance?.instanceId ?: return
        for (mutableEntry in mEventInstanceIdMap) {
            if (mutableEntry.value.isNotEmpty()) {
                mutableEntry.value.remove(nonNullId)
            }
        }
    }

    fun unRegisterEvent(event: String?, instantId: String?) {
        val nonNullId = instantId ?: return
        if (event.isNullOrBlank()) {
            // 删除本页面所有的事件
            for (mutableEntry in mEventInstanceIdMap) {
                if (mutableEntry.value.isNotEmpty()) {
                    mutableEntry.value.remove(nonNullId)
                }
            }
        } else {
            // 删除本页面的指定事件
            val mutableSet = mEventInstanceIdMap[event]
            mutableSet?.remove(nonNullId)
        }
    }

    // 注册接受某事件
    // weex
    // event.registerEvent('myEvent')
    // globalEvent.addEventListener('myEvent', (params) => {});
    fun registerEvent(event: String?, instantId: String?) {
        if (instantId == null) {
            log("registerEvent error mInstId = null")
            return
        }
        val nonNullEvent = event ?: return
        val registerInstantIds = mEventInstanceIdMap[nonNullEvent] ?: mutableSetOf()
        registerInstantIds.add(instantId)
        mEventInstanceIdMap[nonNullEvent] = registerInstantIds
    }

    // 发送事件
    // weex
    // event.post('myEvent',{isOk:true});
    fun postEvent(event: String, params: Map<String, Any>) {
        if (WXSDKManager.getInstance() == null) {
            log("post event WXSDKManager.getInstance() == null")
            return
        }
        val renderManager = WXSDKManager.getInstance().wxRenderManager
        if (renderManager == null) {
            log("post event WXSDKManager.getInstance().wxRenderManager == null")
            return
        }
        val registerInstantIds = mEventInstanceIdMap[event] ?: listOf<String>()
        val allInstants = renderManager.allInstances
        for (instance in allInstants) {
            // 该事件被该 instant 注册过
            if (instance != null
                    && !instance.instanceId.isNullOrEmpty()
                    && registerInstantIds.contains(instance.instanceId)) {
                instance.fireGlobalEventCallback(event, params)
            }
        }
    }

}