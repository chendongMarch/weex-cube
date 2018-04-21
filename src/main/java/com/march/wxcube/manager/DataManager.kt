package com.march.wxcube.manager

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/18
 * Describe : 数据中转
 *
 * @author chendong
 */
class DataManager : IManager {

    companion object {
        val instance: DataManager by lazy { DataManager() }
    }

    private val cacheData: MutableMap<String, JSONObject> = mutableMapOf()


    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
        cacheData.remove(weexPage?.webUrl)
    }

    fun putData(url: String, data: JSONObject) {
        cacheData[url] = data
    }

    fun getData(url: String): JSONObject? {
        val data = cacheData[url]
        cacheData.remove(url)
        return data
    }

}