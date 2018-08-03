package com.march.wxcube.module.dispatcher

import com.bumptech.glide.Glide
import com.march.gallery.ui.EntryDialogFragment
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/6/20
 * Describe : 图片处理
 *
 * @author chendong
 */
class ImageDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun preloadImage(args: WxArgs) {
        val jsonArray = args.params.getJSONArray(KEY_LIST) ?: throw RuntimeException("Image#preload list is null")
        for (obj in jsonArray) {
            if (obj is String) {
                val url = WxUtils.rewriteUrl(obj, URIAdapter.IMAGE)
                Glide.with(mProvider.activity()).load(url).preload()
            }
        }
        }

    @DispatcherJsMethod
    fun previewImage(args: WxArgs) {
        throw RuntimeException("Image#preview not implement")
    }

    @DispatcherJsMethod(async = true)
    fun selectImage(args: WxArgs) {
        val maxNum = args.params.getDef("maxNum", 1)
        val crop = args.params.getDef("crop", false)
        val dialog = EntryDialogFragment.newInst(maxNum, crop)
        dialog.setResultListener {
            if (it != null && it.size > 0) {
                args.callback(mapOf(
                        KEY_SUCCESS to true,
                        KEY_RESULT to mapOf(KEY_LIST to it.map { path -> "file://$path" })))
            } else {
                args.callback(mapOf(KEY_SUCCESS to false, KEY_MSG to "选择图片 size ${it?.size}"))
            }
        }
        dialog.show(mProvider.activity().supportFragmentManager, EntryDialogFragment::class.java.simpleName)
    }

    @DispatcherJsMethod
    fun uploadImage(args: WxArgs) {
        throw RuntimeException("Image#upload not implement")
    }
}