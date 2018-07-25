package com.march.wxcube.manager

import android.content.Context
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.CubeWx
import com.march.wxcube.common.JsonSyncMgr
import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance
import java.lang.Exception

/**
 * CreateAt : 2018/7/13
 * Describe :
 *
 * @author chendong
 */
class OnlineCfgManager : IManager {

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {

    }

    companion object {
        const val KEY = "online-config"
    }

    private val jsonSyncMgr by lazy {
        val cfg = JsonSyncMgr.SyncCfg(KEY, CubeWx.mWxCfg.onlineCfgUrl)
        JsonSyncMgr(cfg) { _, json ->
            parseJsonAndUpdate(json)
        }
    }

    val configs by lazy { mutableMapOf<String, String>() }

    fun update(ctx: Context) {
        jsonSyncMgr.update(ctx)
    }

    private fun parseJsonAndUpdate(json: String?): Boolean {
        if (json == null || json.isBlank()) {
            return false
        }
        try {
            val jsonObj = JSONObject.parseObject(json)
            val map = jsonObj.getJSONObject("data").toMutableMap()
            map.forEach { (key,value) ->
                configs[key] = value.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    fun readItem(key: String): Any? {
        return configs[key]
    }


}
