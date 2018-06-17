package com.march.wxcube.wxadapter

import android.widget.ImageView

import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.march.common.utils.LgUtils
import com.march.wxcube.manager.ManagerRegistry
import com.taobao.weex.adapter.IWXImgLoaderAdapter
import com.taobao.weex.common.WXImageStrategy
import com.taobao.weex.dom.WXImageQuality


@GlideModule
class MyAppGlideModule : AppGlideModule()

/**
 * CreateAt : 2018/3/26
 * Describe : 图片加载
 *
 * @author chendong
 */
class ImgAdapter : IWXImgLoaderAdapter {
    override fun setImage(url: String?, view: ImageView?, quality: WXImageQuality?, strategy: WXImageStrategy?) {
        if (view != null && url != null) {
            val safeUrl = ManagerRegistry.HOST.makeRequestUrl(url)
            LgUtils.e("${view.measuredWidth}  ${view.measuredHeight}")
            GlideApp.with(view.context).load(safeUrl).into(view)
        }
    }
}
