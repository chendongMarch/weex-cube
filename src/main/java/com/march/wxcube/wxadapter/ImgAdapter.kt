package com.march.wxcube.wxadapter

import android.widget.ImageView

import com.bumptech.glide.Glide
import com.taobao.weex.adapter.IWXImgLoaderAdapter
import com.taobao.weex.common.WXImageStrategy
import com.taobao.weex.dom.WXImageQuality

/**
 * CreateAt : 2018/3/26
 * Describe : 图片加载
 *
 * @author chendong
 */
class ImgAdapter : IWXImgLoaderAdapter {
    override fun setImage(url: String?, view: ImageView?, quality: WXImageQuality?, strategy: WXImageStrategy?) {
        if (view != null && url != null) {
            Glide.with(view.context).load(url).into(view)
        }
    }
}
