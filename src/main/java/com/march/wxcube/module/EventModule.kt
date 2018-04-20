package com.march.wxcube.module

import com.march.wxcube.manager.EventManager
import com.march.wxcube.manager.ManagerRegistry
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/4/18
 * Describe : 事件
 *
 * @author chendong
 */
class EventModule : WXModule() {

    // 注册接受某事件
    // weex
    // event.registerEvent('myEvent')
    // globalEvent.addEventListener('myEvent', (params) => {});
    @JSMethod(uiThread = true)
    fun registerEvent(key: String?) {
        ManagerRegistry.EVENT.registerEvent(key, instantId)
    }

    // 发送事件
    // weex
    // event.post('myEvent',{isOk:true});
    @JSMethod(uiThread = true)
    fun postEvent(key: String, params: Map<String, Any>) {
        ManagerRegistry.EVENT.postEvent(key, params)
    }

}