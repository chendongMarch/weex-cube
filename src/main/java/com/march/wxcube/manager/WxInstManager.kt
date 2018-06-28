package com.march.wxcube.manager

import com.march.wxcube.model.WxPage
import com.march.wxcube.ui.WxDelegate
import com.taobao.weex.WXSDKInstance
import java.lang.ref.WeakReference

/**
 * CreateAt : 2018/5/3
 * Describe : 存储 WeexDelegate，可以借助 mInstId 获取到指定 WeexDelegate
 *
 * @author chendong
 */
class WxInstManager : IManager {

    companion object {
        val instance by lazy { WxInstManager() }
    }
    private val mWeexDelegateMap by lazy { mutableMapOf<String, WeakReference<WxDelegate>>() }
    private val mWeexActivityMap by lazy { mutableMapOf<String, WeakReference<WxDelegate>>() }

    override fun onWxInstInit(weexPage: WxPage?, instance: WXSDKInstance?, weexDelegate: WxDelegate?) {
        super.onWxInstInit(weexPage, instance, weexDelegate)
        if (instance == null && instance?.instanceId == null || weexDelegate == null) {
            return
        }
        mWeexDelegateMap[instance.instanceId] = WeakReference(weexDelegate)
    }

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {
        if (instance == null && instance?.instanceId == null) {
            return
        }
        mWeexDelegateMap.remove(instance.instanceId)
    }

    fun findWeexDelegateByInstanceId(instanceId: String): WxDelegate? {
        return mWeexDelegateMap[instanceId]?.get()
    }
}