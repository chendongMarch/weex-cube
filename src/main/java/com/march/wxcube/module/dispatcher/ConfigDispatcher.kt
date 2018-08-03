package com.march.wxcube.module.dispatcher

import com.march.wxcube.CubeWx
import com.march.wxcube.common.getDef
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs

/**
 * CreateAt : 2018/7/15
 * Describe : 配置
 *
 * @author chendong
 */
class ConfigDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun updateOnlineConfig(args: WxArgs) {
        ManagerRegistry.OnlineCfg.update(mProvider.activity())
        args.callback(mapOf(KEY_SUCCESS to true))
    }

    @DispatcherJsMethod
    fun updateWeexConfig(args: WxArgs) {
        CubeWx.mWxUpdater.update(mProvider.activity())
        args.callback(mapOf(KEY_SUCCESS to true))
    }


    @DispatcherJsMethod
    fun readOnlineConfigItem(args: WxArgs) {
        val key = args.params.getDef(KEY_KEY, "")
        if (key.isBlank()) {
            args.callback(mapOf(KEY_SUCCESS to false, KEY_MSG to "key is empty ${args.params}"))
        } else {
            val item = ManagerRegistry.OnlineCfg.readItem(key)
            if (item == null) {
                args.callback(mapOf(KEY_SUCCESS to false, KEY_MSG to "not get item"))
            } else {
                args.callback(mapOf(KEY_SUCCESS to true, KEY_RESULT to mapOf(KEY_DATA to item)))
            }
        }
    }

    @DispatcherJsMethod
    fun readAllOnlineConfig(args: WxArgs) {
        args.callback(mapOf(KEY_SUCCESS to true,
                KEY_RESULT to mapOf(KEY_DATA to ManagerRegistry.OnlineCfg.configs)))
    }

    @DispatcherJsMethod
    fun readAllWeexConfig(args: WxArgs) {
        val map = CubeWx.mWxRouter.mWeexPageMap
        args.callback(mapOf(KEY_SUCCESS to true, KEY_RESULT to mapOf(KEY_DATA to map.values)))
    }
}