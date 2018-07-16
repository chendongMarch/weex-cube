package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.CubeWx
import com.march.wxcube.common.getDef
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/7/15
 * Describe :
 *
 * @author chendong
 */
class ConfigDispatcher : BaseDispatcher() {

    companion object {
        const val updateOnlineConfig = "updateOnlineConfig" // 更新在线参数
        const val updateWeexConfig = "updateWeexConfig" // 更新 weex 配置
        const val readOnlineConfigItem = "readOnlineConfigItem" // 读取一项在线参数
        const val readAllOnlineConfig = "readAllOnlineConfig" // 读取全部在线参数
        const val readAllWeexConfig = "readAllWeexConfig" // 读取全部 weex 配置表
    }

    override fun getMethods(): Array<String> {
        return arrayOf(updateOnlineConfig,
                updateWeexConfig,
                readOnlineConfigItem,
                readAllOnlineConfig,
                readAllWeexConfig)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        when (method) {
            updateOnlineConfig   -> updateOnlineConfig(jsCallbackWrap)
            updateWeexConfig     -> updateWeexConfig(jsCallbackWrap)
            readOnlineConfigItem -> readOnlineConfigItem(params, jsCallbackWrap)
            readAllOnlineConfig  -> readAllOnlineConfig(jsCallbackWrap)
            readAllWeexConfig    -> readAllWeexConfig(jsCallbackWrap)
        }
    }

    private fun updateOnlineConfig(jsCallbackWrap: JsCallbackWrap) {
        ManagerRegistry.OnlineCfg.update(mProvider.activity())
        jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to true))
    }

    private fun updateWeexConfig(jsCallbackWrap: JsCallbackWrap) {
        CubeWx.mWxUpdater.update(mProvider.activity())
        jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to true))
    }


    private fun readOnlineConfigItem(params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val key = params.getDef(KEY_KEY, "")
        if (key.isBlank()) {
            jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to false, KEY_MSG to "key is empty $params"))
        } else {
            val item = ManagerRegistry.OnlineCfg.readItem(key)
            if (item == null) {
                jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to false, KEY_MSG to "not get item"))
            } else {
                jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to true, KEY_RESULT to item))
            }
        }
    }

    private fun readAllOnlineConfig(jsCallbackWrap: JsCallbackWrap) {
        jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to true,
                KEY_RESULT to ManagerRegistry.OnlineCfg.configs))
    }

    private fun readAllWeexConfig(jsCallbackWrap: JsCallbackWrap) {
        val map = CubeWx.mWxRouter.mWeexPageMap
        jsCallbackWrap.invoke(mapOf(KEY_SUCCESS to true,
                KEY_RESULT to map.values))
    }
}