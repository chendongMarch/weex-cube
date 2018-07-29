package com.march.wxcube.manager

import com.march.wxcube.CubeWx
import com.march.wxcube.common.log
import com.march.wxcube.func.router.UrlKey
import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/7/29
 * Describe : 用来做 weex 对本地资源的映射
 *
 * @author chendong
 */
class ResMappingMgr : IManager {

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {

    }

    // url 和 包内资源的映射
    private val urlResMap by lazy { mutableMapOf<UrlKey, Int>() }

    init {
        val resMap = CubeWx.mWxInitAdapter.getUrlResMap()
        for (entry in resMap) {
            registerUrlRes(entry.key, entry.value)
        }
    }

    fun registerUrlRes(url: String, res: Int) {
        urlResMap[UrlKey.fromUrl(url)] = res
    }

    fun mapUrlRes(url: String?): Int {
        if (url == null) {
            return -1
        }
        val key = UrlKey.fromUrl(url)
        if (key.host == null || key.path == null) {
            return -1
        }
        val res = urlResMap[key] ?: -1
        if (res != -1) {
            log("获取到资源映射 $url -> $res")
        }
        return res
    }
}