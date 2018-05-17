package com.march.wxcube.loading

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.taobao.weex.WXSDKManager

/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
open class LoadingHandlerImpl : ILoadingHandler {

    private var loadingView: View? = null

    override fun finish(container: ViewGroup?) {
        loadingView
                ?.animate()
                ?.alpha(0f)
                ?.setDuration(500L)
                ?.start()
        container?.postDelayed({
            loadingView?.visibility = View.GONE
            container.removeView(loadingView)
        }, 500)
    }

    override fun addLoadingView(container: ViewGroup?) {
        if (container != null && container.context != null) {
            if (loadingView == null) {
                loadingView = LayoutInflater.from(container.context).inflate(R.layout.widget_loading, container, false)
            }
            container.addView(loadingView)
        }
    }

    override fun setIndexPageContent(activity: Activity) {
        val layout = FrameLayout(activity)
        layout.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        val imageView = ImageView(activity)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        WXSDKManager.getInstance().iwxImgLoaderAdapter
                .setImage("http://olx4t2q6z.bkt.clouddn.com/18-2-1/44700615.jpg", imageView, null, null)
        layout.addView(imageView)
        activity.setContentView(layout)
    }

}