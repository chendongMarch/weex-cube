package com.march.wxcube.module.dispatcher

import com.march.wxcube.common.WxUtils
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam
import com.march.wxcube.wxadapter.GlideApp
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/6/20
 * Describe : 图片处理
 *
 * @author chendong
 */
class ImageDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun preloadImage(param: DispatcherParam) {
        val jsonArray = param.params.getJSONArray(KEY_LIST) ?: throw RuntimeException("Image#preload list is null")
        for (obj in jsonArray) {
            if (obj is String) {
                val url = WxUtils.rewriteUrl(obj, URIAdapter.IMAGE)
                GlideApp.with(mProvider.activity()).load(url).preload()
            }
        }
    }

    @DispatcherJsMethod
    fun previewImage(param: DispatcherParam) {
        throw RuntimeException("Image#preview not implement")
    }

    @DispatcherJsMethod
    fun selectImage(param: DispatcherParam) {
        throw RuntimeException("Image#select not implement")
    }

    @DispatcherJsMethod
    fun uploadImage(param: DispatcherParam) {
        throw RuntimeException("Image#upload not implement")
    }
}