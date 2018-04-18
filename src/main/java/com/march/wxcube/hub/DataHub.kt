package com.march.wxcube.hub

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.model.WeexPage

/**
 * CreateAt : 2018/4/18
 * Describe : 数据中转
 *
 * @author chendong
 */
object DataHub {

    private val cacheData: MutableMap<String, JSONObject> = mutableMapOf()

    fun clear(weexPage: WeexPage) {
        cacheData.remove(weexPage.webUrl)
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