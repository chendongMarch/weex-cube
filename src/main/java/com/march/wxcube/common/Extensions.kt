package com.march.wxcube.common

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.march.common.model.WeakContext
import com.march.wxcube.CubeWx
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.wxadapter.UriWriter
import com.taobao.weex.adapter.URIAdapter
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
    CubeWx.mWxReportAdapter.log(this::class.java.simpleName, msg, throwable)
}

fun Context.weak(): WeakContext {
    return WeakContext(this)
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
    val request = Glide.with(this).asBitmap()
    val res = ManagerRegistry.ResMapping.mapUrlRes(path)
    if (res > 0) {
        request.load(res)
    } else {
        request.load(UriWriter.rewrite(path, URIAdapter.IMAGE))
    }.into(object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            resolver(resource)
        }
    })
}

fun Any.tag(): String {
    return this::class.java.simpleName
}