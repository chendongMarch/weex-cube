package com.march.wxcube.common

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.march.common.adapter.JsonParser
import java.lang.reflect.Type

/**
 * CreateAt : 2018/4/3
 * Describe :
 *
 * @author chendong
 */
class JsonParserImpl : JsonParser {

    private class MyTypeRef<T>(vararg actualTypeArguments: Type?) : com.alibaba.fastjson.TypeReference<T>(*actualTypeArguments)

    override fun <T : Any?> toObj(json: String, cls: Class<T>?): T {
        return JSONObject.parseObject(json, cls)
    }

    override fun <T : Any?> toList(json: String, clazz: Class<T>): MutableList<T> {
        return JSONArray.parseArray(json, clazz)
    }

    override fun <K : Any?, V : Any?> toMap(json: String, kClazz: Class<K>, vClazz: Class<V>): MutableMap<K, V> {
        return JSONObject.parseObject(json, MyTypeRef<Map<String, String>>(kClazz, vClazz).type)
    }

    override fun toJson(`object`: Any?): String {
        return JSON.toJSONString(`object`)
    }

}