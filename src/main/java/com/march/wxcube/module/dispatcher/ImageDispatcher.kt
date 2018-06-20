package com.march.wxcube.module.dispatcher

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.JsCallbackWrap
import com.march.wxcube.wxadapter.GlideApp

/**
 * CreateAt : 2018/6/20
 * Describe :
 *
 * @author chendong
 */
class ImageDispatcher : BaseDispatcher() {

    companion object {
        const val preloadImage = "preloadImage"
        const val previewImage = "previewImage"
        const val selectImage = "selectImage"
        const val uploadImage = "uploadImage"
    }

    override fun getMethods(): Array<String> {
        return arrayOf(
                preloadImage,
                previewImage,
                selectImage,
                uploadImage)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        when (method) {
            preloadImage -> preloadImage(params, jsCallbackWrap)
            previewImage -> previewImage(params, jsCallbackWrap)
            selectImage  -> selectImage(params, jsCallbackWrap)
            uploadImage  -> uploadImage(params, jsCallbackWrap)
        }
    }

    private fun preloadImage(params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val url = params.getString(KEY_URL) ?: throw RuntimeException("Image#preload url is null")
        GlideApp.with(mProvider.activity()).load(url).preload()
    }

    private fun previewImage(params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        throw RuntimeException("Image#preview not implement")
    }

    private fun selectImage(params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        throw RuntimeException("Image#select not implement")
    }

    private fun uploadImage(params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        throw RuntimeException("Image#upload not implement")
    }
}