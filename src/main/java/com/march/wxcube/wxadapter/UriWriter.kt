package com.march.wxcube.wxadapter

import com.march.wxcube.CubeWx
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
object UriWriter {

    fun rewrite(url: String, type: String): String {
        var modifyUrl = url
        if (type == URIAdapter.REQUEST || type == URIAdapter.WEB) {
            val host = if (type == URIAdapter.REQUEST) CubeWx.mWxCfg.reqAuthority else CubeWx.mWxCfg.webAuthority
            modifyUrl = addAuthority(modifyUrl, host)
        }
        modifyUrl = addHttpSchema(modifyUrl)
        return modifyUrl
    }

    // 替换 https 为 http
    private fun replaceHttpSchema(url: String): String {
        return if (url.startsWith("https")) {
            url.replace("https", "http")
        } else url
    }

    // 检测添加 schema
    // 以 // 开头没有
    private fun addHttpSchema(url: String): String {
        return if (url.startsWith("//")) {
            val schema = if (CubeWx.mWxCfg.https) "https:" else "http:"
            "$schema$url"
        } else url
    }

    // 添加 authority
    // 以 / 开头为没有 auth
    private fun addAuthority(url: String, host: String): String {
        return if (url.startsWith("/") && !url.startsWith("//")) {
            "//$host$url"
        } else url
    }
}
