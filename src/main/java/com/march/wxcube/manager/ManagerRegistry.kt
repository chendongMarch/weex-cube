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

        val Event by lazy { getInst().get(EventBusMgr::class.java) as EventBusMgr }
        val Data by lazy { getInst().get(MemoryDataMgr::class.java) as MemoryDataMgr }
        val Request by lazy { getInst().get(RequestMgr::class.java) as RequestMgr }
        val OnlineCfg by lazy { getInst().get(OnlineCfgMgr::class.java) as OnlineCfgMgr }
        val WxInst by lazy { getInst().get(WxInstMgr::class.java) as WxInstMgr }
        val ResMapping by lazy { getInst().get(ResMappingMgr::class.java) as ResMappingMgr }
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