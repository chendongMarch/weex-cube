package com.march.wxcube.manager

import com.march.wxcube.Weex
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

    fun validUrl(url: String?): String {
        if (url == null) {
            return ""
        }
        var mutableUrl = url
        if (mutableUrl.startsWith("http")) {
            if (!Weex.getInst().mWeexConfig.https) {
                mutableUrl = mutableUrl.replace("https","http",true)
            }
        }
        if (mutableUrl.contains("//")) {
            return mutableUrl
        }
        val host = mEnvHostMap[mNowEnv] ?: return mutableUrl

        if (!mutableUrl.startsWith("/")) {
            mutableUrl = "/$mutableUrl"
        }
        return "$host$mutableUrl"
    }


    fun safeUrl(url: String?): String {
        var mutableUrl = url ?: return ""
        mutableUrl = validUrl(mutableUrl)
        if (mutableUrl.startsWith("//")) {
            mutableUrl = "http:$mutableUrl"
        }
        return mutableUrl
    }

}