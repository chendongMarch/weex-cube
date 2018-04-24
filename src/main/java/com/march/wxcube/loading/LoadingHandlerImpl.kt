package com.march.wxcube.loading

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.march.wxcube.R

/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
class LoadingHandlerImpl : ILoadingHandler {

    private var loadingView: View? = null

    override fun finish(container: ViewGroup?) {
        container?.postDelayed({
            loadingView?.visibility = View.GONE
            container.removeView(loadingView)
        }, 300)
    }

    override fun addLoadingView(container: ViewGroup?) {
        if (container != null && container.context != null) {
            if (loadingView == null) {
                loadingView = LayoutInflater.from(container.context).inflate(R.layout.widget_loading, container, false)
            }
            container.addView(loadingView)
        }
    }
}