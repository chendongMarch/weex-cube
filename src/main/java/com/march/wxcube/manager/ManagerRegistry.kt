package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
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

        val EVENT by lazy { getInst().get(EventManager::class.java) as EventManager }
        val DATA by lazy { getInst().get(DataManager::class.java) as DataManager }
        val HTTP by lazy { getInst().get(HttpManager::class.java) as HttpManager }
        val ENV by lazy { getInst().get(EnvManager::class.java) as EnvManager }
    }

    private val mManagerMap by lazy { mutableMapOf<String, IManager>() }

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
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