package com.march.wxcube.module.dispatcher

import com.march.wxcube.CubeWx
import com.march.wxcube.common.getDef
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam

/**
 * CreateAt : 2018/7/15
 * Describe : 配置
 *
 * @author chendong
 */
class ConfigDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun updateOnlineConfig(param: DispatcherParam) {
        ManagerRegistry.OnlineCfg.update(mProvider.activity())
        param.callback(mapOf(KEY_SUCCESS to true))
    }

    @DispatcherJsMethod
    fun updateWeexConfig(param: DispatcherParam) {
        CubeWx.mWxUpdater.update(mProvider.activity())
        param.callback(mapOf(KEY_SUCCESS to true))
    }


    @DispatcherJsMethod
    fun readOnlineConfigItem(param: DispatcherParam) {
        val key = param.params.getDef(KEY_KEY, "")
        if (key.isBlank()) {
            param.callback(mapOf(KEY_SUCCESS to false, KEY_MSG to "key is empty ${param.params}"))
        } else {
            val item = ManagerRegistry.OnlineCfg.readItem(key)
            if (item == null) {
                param.callback(mapOf(KEY_SUCCESS to false, KEY_MSG to "not get item"))
            } else {
                param.callback(mapOf(KEY_SUCCESS to true, KEY_RESULT to mapOf(KEY_DATA to item)))
            }
        }
    }

    @DispatcherJsMethod
    fun readAllOnlineConfig(param: DispatcherParam) {
        param.callback(mapOf(KEY_SUCCESS to true,
                KEY_RESULT to mapOf(KEY_DATA to ManagerRegistry.OnlineCfg.configs)))
    }

    @DispatcherJsMethod
    fun readAllWeexConfig(param: DispatcherParam) {
        val map = CubeWx.mWxRouter.mWeexPageMap
        param.callback(mapOf(KEY_SUCCESS to true, KEY_RESULT to mapOf(KEY_DATA to map.values)))
    }
}