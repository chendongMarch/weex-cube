package com.march.wxcube.wxadapter

import android.graphics.Bitmap
import android.widget.ImageView
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.march.wxcube.CubeWx
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
            var request = GlideApp.with(view.context)
                    .asBitmap()
                    .load(url)
                    .listener(RequestListenerImpl(url, view, strategy))
            if (view.measuredWidth > 0 && view.measuredHeight > 0) {
                request = request.override(view.measuredWidth, view.measuredHeight)
                if (view.measuredWidth > 300 && CubeWx.mWeexConfig.largeImgHolder > 0) {
                    request =  request.placeholder(CubeWx.mWeexConfig.largeImgHolder).error(CubeWx.mWeexConfig.largeImgHolder)
                } else if (CubeWx.mWeexConfig.smallImgHolder > 0) {
                    request = request.placeholder(CubeWx.mWeexConfig.smallImgHolder).error(CubeWx.mWeexConfig.smallImgHolder)
                }
            }
            request.into(view)
        } else {
            strategy?.imageListener?.onImageFinish(url, view, false, null)
        }
    }

    class RequestListenerImpl(
            private val url: String?,
            private val view: ImageView?,
            private val strategy: WXImageStrategy?) : RequestListener<Bitmap> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
            strategy?.imageListener?.onImageFinish(url, view, false, null)
            return false
        }

        override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            view?.setImageBitmap(resource)
            strategy?.imageListener?.onImageFinish(url, view, true, null)
            return false
        }
    }
}
