package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDelegate
import com.taobao.weex.WXSDKInstance
import java.lang.ref.WeakReference

/**
 * CreateAt : 2018/5/3
 * Describe : 存储 WeexDelegate，可以借助 instantId 获取到指定 WeexDelegate
 *
 * @author chendong
 */
class WeexInstManager : IManager {

    companion object {
        val instance by lazy { WeexInstManager() }
    }
    private val mWeexDelegateMap by lazy { mutableMapOf<String, WeakReference<WeexDelegate>>() }

    override fun onWxInstInit(weexPage: WeexPage?, instance: WXSDKInstance?, weexDelegate: WeexDelegate?) {
        super.onWxInstInit(weexPage, instance, weexDelegate)
        if (instance == null && instance?.instanceId == null || weexDelegate == null) {
            return
        }
        mWeexDelegateMap[instance.instanceId] = WeakReference(weexDelegate)
    }

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
        if (instance == null && instance?.instanceId == null) {
            return
        }
        mWeexDelegateMap.remove(instance.instanceId)
    }

    fun findWeexDelegateByInstanceId(instanceId: String): WeexDelegate? {
        return mWeexDelegateMap[instanceId]?.get()
    }
}