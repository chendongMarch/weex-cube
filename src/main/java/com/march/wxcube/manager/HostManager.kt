package com.march.wxcube.manager

import com.march.wxcube.Weex
import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/21
 * Describe :
 *   拼接
 *   configUrl http://www.config.test.com/weex-config 就这一个，直接配死
 *   webUrl   /home/test-weex.html 自动添加host、但是不添加schema、加载网页时再添加 schema
 *   openUrl() /home/test-weex.html 自动添加host、但是不添加schema、加载网页时再添加 schema
 *   reqUrl  /list/data  请求的url，发起请求时，添加schema/host
 *   jsUrl /home/test-weex.js 最需要被调试，host 定制
 *
 *   @author chendong
 */
class HostManager : IManager {

    companion object {
        val instance: HostManager by lazy { HostManager() }
    }

    // host 不带 http,以 // 开头

    var mApiHost: String = ""
    var mJsResHost: String = ""
    var mWebHost: String = ""

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {

    }

    // 检测添加 schema
    private fun addSchema(url: String): String {
        if (url.isBlank()) {
            return url
        }
        var mutableUrl = url
        if (mutableUrl.startsWith("//")) {
            val scheme = if (Weex.mWeexConfig.https) "https" else "http"
            mutableUrl = "$scheme:$mutableUrl"
        } else if (mutableUrl.startsWith("https")) {
            if (!Weex.mWeexConfig.https) {
                mutableUrl = mutableUrl.replace("https", "http")
            }
        } else if (mutableUrl.startsWith("http")) {
            if (Weex.mWeexConfig.https) {
                mutableUrl = mutableUrl.replace("http", "https")
            }
        }
        return mutableUrl
    }


    // 相对的请求 url 需要以 / 开头
    // 检测完善一个请求的url
    fun makeRequestUrl(url: String): String {
        if (url.isBlank()) {
            return url
        }
        var mutableUrl = url
        // add host
        if (mutableUrl.startsWith("/")) {
            mutableUrl = "$mApiHost$mutableUrl"
        }
        return addSchema(mutableUrl)
    }

    // webUrl | openUrl() 使用时需要 / 开头
    fun makeWebUrl(url: String): String {
        if (url.isBlank()) {
            return url
        }
        var mutableUrl = url
        // add host
        if (mutableUrl.startsWith("/")) {
            mutableUrl = "$mWebHost$mutableUrl"
        }
        return addSchema(mutableUrl)
    }


    // webUrl | openUrl() 使用时需要 / 开头
    fun makeJsResUrl(url: String): String {
        if (url.isBlank()) {
            return url
        }
        var mutableUrl = url
        // add host
        if (mutableUrl.startsWith("/")) {
            mutableUrl = "$mJsResHost$mutableUrl"
        }
        return addSchema(mutableUrl)
    }
}