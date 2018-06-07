package com.march.wxcube.common

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import java.lang.Exception

/**
 * CreateAt : 2018/6/6
 * Describe :
 *
 * @author chendong
 */

// jsonObj -> Obj
fun <T> JSONObject?.toObjEx(clz: Class<T>): T? {
    if (this == null) {
        return null
    }
    val json = JSON.toJSONString(this)
    return JSONObject.parseObject(json, clz)
}

// jsonArray -> List
fun <T> JSONArray?.toListEx(clz: Class<T>): List<T>? {
    if (this == null) {
        return null
    }
    val json = JSON.toJSONString(this)
    return JSONArray.parseArray(json, clz)
}

inline fun <reified T> JSONObject.getDef(key: String, def: T): T {
    if (!containsKey(key)) {
        return def
    }
    return try {
        when (def) {
            is Boolean -> getBoolean(key) as? T ?: def
            is Int     -> getInteger(key) as? T ?: def
            is String  -> getString(key) as? T ?: def
            is Long    -> getLong(key) as? T ?: def
            is Float   -> getFloat(key) as? T ?: def
            else       -> def
        }
    } catch (e: Exception) {
        e.printStackTrace()
        def
    }
}
