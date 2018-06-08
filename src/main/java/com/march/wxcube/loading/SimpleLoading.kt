package com.march.wxcube.loading

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.march.common.utils.DrawableUtils
import com.march.wxcube.R
import com.march.wxcube.ui.IndexActivity
import com.taobao.weex.WXSDKManager

/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
interface Loading {

    // weex 页面渲染之前的 loading
    fun startWeexLoading(container: ViewGroup?)

    // 结束 weex 页面渲染的 loading
    fun finishWeexLoading(container: ViewGroup?)

    // 设置首页的内容，这里需要是原生的
    fun setIndexContent(activity: IndexActivity)

    fun makeLoadingIndicator(activity: Activity): View

}

open class SimpleLoading : Loading {

    private var weexLoadingView: View? = null

    override fun finishWeexLoading(container: ViewGroup?) {
        weexLoadingView
                ?.animate()
                ?.alpha(0f)
                ?.setDuration(500L)
                ?.start()
        container?.postDelayed({
            weexLoadingView?.visibility = View.GONE
            container.removeView(weexLoadingView)
        }, 500)
    }

    override fun startWeexLoading(container: ViewGroup?) {
        if (container != null && container.context != null) {
            if (weexLoadingView == null) {
                weexLoadingView = LayoutInflater
                        .from(container.context)
                        .inflate(R.layout.widget_loading, container, false)
            }
            container.addView(weexLoadingView)
        }
    }

    override fun setIndexContent(activity: IndexActivity) {
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

    override fun makeLoadingIndicator(activity: Activity): View {
        val frameLayout = FrameLayout(activity)
        frameLayout.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val drawable = DrawableUtils.newRoundRectDrawable(Color.parseColor("#66000000"), 5)
        frameLayout.background = drawable
        frameLayout.setPadding(50,50,50,50)
        val progressBar = ProgressBar(activity)
        progressBar.layoutParams = FrameLayout.LayoutParams(100, 100)
        val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        frameLayout.addView(progressBar, layoutParams)
        return frameLayout
    }
}