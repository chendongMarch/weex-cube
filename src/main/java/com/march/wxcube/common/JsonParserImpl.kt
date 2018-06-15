package com.march.wxcube.common

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.march.common.adapter.JsonParser

/**
 * CreateAt : 2018/4/3
 * Describe :
 *
 * @author chendong
 */
class JsonParserImpl : JsonParser {

    override fun <T : Any?> toObj(json: String?, cls: Class<T>?): T {

        return JSONObject.parseObject(json, cls)
    }

    override fun <T : Any?> toList(json: String?): MutableList<T> {
        return JSON.parseObject(json, object : TypeReference<MutableList<T>>() {

        })
    }

    override fun <K : Any?, V : Any?> toMap(json: String?): MutableMap<K, V> {
        return JSON.parseObject(json, object : TypeReference<HashMap<K, V>>() {

        })
    }

    override fun toJson(`object`: Any?): String {
        return JSON.toJSONString(`object`)
    }

}