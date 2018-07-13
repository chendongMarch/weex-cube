package com.march.wxcube.common

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.march.wxcube.CubeWx
import com.march.wxcube.wxadapter.GlideApp
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

// 提交错误
fun Any.log(msg: String, throwable: Throwable? = null) {
    if (CubeWx.mWxCfg.debug) {
        CubeWx.mWxReportAdapter.log(this::class.java.simpleName, msg, throwable)
    }
}


fun Context.memory(float: Float): Int {
    val activityManager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return (activityManager.memoryClass * 1024 * 1024 * float).toInt()
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

fun Context.downloadImage(path: String, resolver: (Bitmap) -> Unit) {
    GlideApp.with(this).asBitmap().load(path)
            .into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    resolver(resource)
                }
            })
}