package com.march.wxcube.common

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.view.View
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import java.io.File
import java.lang.Exception
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * CreateAt : 2018/4/18
 * Describe :
 *
 * @author chendong
 */
// jsonObj -> Obj
fun <T> JSONObject?.toObj(clz: Class<T>): T? {
    if (this == null) {
        return null
    }
    val json = JSON.toJSONString(this)
    return JSONObject.parseObject(json, clz)
}

// jsonArray -> List
fun <T> JSONArray?.toList(clz: Class<T>): List<T> {
    if (this == null) {
        return listOf()
    }
    val json = JSON.toJSONString(this)
    return JSONArray.parseArray(json, clz)
}

// 提交错误
fun Any.report(msg: String, throwable: Throwable? = null) {
    if (Weex.getInst().mWeexConfig.debug) {
        Weex.getInst().mWeexInjector.onErrorReport(throwable, msg)
    }
}

fun Context.memory(float: Float): Int {
    val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return (activityManager.memoryClass * 1024 * 1024 * float).toInt()
}

fun Any.sdFile(): File {
    return Environment.getExternalStorageDirectory();
}

fun String.md5(): String {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val md5Data = BigInteger(1, md.digest(toByteArray(Charset.forName("utf-8"))))
        String.format("%032X", md5Data)
    } catch (e: Exception) {
        ""
    }
}

fun View?.click(f: (View) -> Unit) {
    this?.setOnClickListener { f(this) }
}

fun StringBuilder.newLine(): StringBuilder {
    return this.append("\n")
}