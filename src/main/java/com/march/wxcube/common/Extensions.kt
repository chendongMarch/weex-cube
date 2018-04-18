package com.march.wxcube.common

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

/**
 * CreateAt : 2018/4/18
 * Describe :
 *
 * @author chendong
 */
// jsonObj -> Obj
fun <T> JSONObject?.toObj(clz: Class<T>): T? {
    if (this == null){
        return null
    }
    val json = JSON.toJSONString(this)
    return JSONObject.parseObject(json, clz)
}

// jsonArray -> List
fun <T> JSONArray?.toList(clz: Class<T>): List<T> {
    if (this == null){
        return listOf()
    }
    val json = JSON.toJSONString(this)
    return JSONArray.parseArray(json, clz)
}
