package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/21
 * Describe :
 *
 * @author chendong
 */
class EnvManager : IManager {

    companion object {
        val instance: EnvManager by lazy { EnvManager() }
    }

    private val mEnvHostMap by lazy { mutableMapOf<String, String>() }
    var mNowEnv: String? = null

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {

    }

    fun registerEnv(mutableMap: Map<String, String>?) {
        if (mutableMap == null) {
            return
        }
        for (mutableEntry in mutableMap) {
            registerEnv(mutableEntry.key, mutableEntry.value)
        }
    }


    fun registerEnv(env: String, host: String) {
        var mutableHost = host
        if (mutableHost.endsWith("/")) {
            mutableHost = host.substring(0, mutableHost.length - 1)
        }
        mEnvHostMap[env] = delHttp(mutableHost)
    }

    fun delHttp(url: String): String {
        if (url.contains("http")) {
            return url.replace("https:", "").replace("http:", "")
        }
        return url
    }


    // for pages.json
    fun checkAddHost(url: String?): String {
        if (url == null) {
            return ""
        }
        if (url.startsWith("http") || url.contains("//")) {
            return url
        }
        var path = url
        val host = mEnvHostMap[mNowEnv] ?: return url

        if (!path.startsWith("/")) {
            path = "/$path"
        }
        return "$host$path"
    }


    fun safeUrl(url: String?): String {
        var mutableUrl = url ?: return ""
        mutableUrl = checkAddHost(mutableUrl)
        if (mutableUrl.startsWith("//")) {
            mutableUrl = "http:$mutableUrl"
        }
        return mutableUrl
    }

}