package com.march.wxcube.manager

import com.march.wxcube.model.WxPage
import com.march.wxcube.ui.WxDelegate
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/20o
 * Describe :
 *
 * @author chendong
 */
class ManagerRegistry : IManager {

    companion object {

        private val instance: ManagerRegistry by lazy { ManagerRegistry() }

        fun getInst() = instance

        val Event by lazy { getInst().get(EventManager::class.java) as EventManager }
        val Data by lazy { getInst().get(DataManager::class.java) as DataManager }
        val Request by lazy { getInst().get(RequestManager::class.java) as RequestManager }
        val Host by lazy { getInst().get(HostManager::class.java) as HostManager }
        val WxInst by lazy { getInst().get(WxInstManager::class.java) as WxInstManager }
    }

    private val mManagerMap by lazy { mutableMapOf<String, IManager>() }

    override fun onWxInstInit(weexPage: WxPage?, instance: WXSDKInstance?, weexDelegate: WxDelegate?) {
        for (mutableEntry in mManagerMap) {
            mutableEntry.value.onWxInstInit(weexPage, instance, weexDelegate)
        }
    }

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {
        for (mutableEntry in mManagerMap) {
            mutableEntry.value.onWxInstRelease(weexPage, instance)
        }
    }

    fun register(manager: IManager) {
        mManagerMap[manager::class.java.simpleName] = manager
    }

    fun <T> get(clazz: Class<T>): IManager? {
        return mManagerMap[clazz.simpleName] ?: throw IllegalStateException("manager not register")
    }

}