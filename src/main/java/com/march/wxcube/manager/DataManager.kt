package com.march.wxcube.manager

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/18
 * Describe : 数据中转
 *
 * @author chendong
 */
class DataManager : IManager {

    companion object {
        val instance: DataManager by lazy { DataManager() }
    }

    private val mCacheExtraData: MutableMap<String, Any> = mutableMapOf()

    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
        mCacheExtraData.remove(weexPage?.webUrl)
    }

    fun putData(url: String, data: Any) {
        mCacheExtraData[url] = data
    }

    fun getData(url: String): Any? {
        val data = mCacheExtraData[url]
        mCacheExtraData.remove(url)
        return data
    }

}