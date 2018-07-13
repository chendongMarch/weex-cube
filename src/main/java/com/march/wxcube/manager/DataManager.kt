package com.march.wxcube.manager

import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/18
 * Describe : 数据中转
 *
 * @author chendong
 */
class DataManager : IManager {

    private val mCacheExtraData: MutableMap<String, Any> = mutableMapOf()

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {
        mCacheExtraData.remove(weexPage?.h5Url)
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