package com.march.wxcube.debug

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.march.common.impl.ActivityLifecycleCallback
import com.march.common.utils.DimensUtils
import com.march.common.utils.ToastUtils
import com.march.common.view.DragLayout
import com.march.wxcube.R

/**
 * CreateAt : 2018/6/15
 * Describe :
 *
 * @author chendong
 */
class WxDebugActivityLifeCycle : ActivityLifecycleCallback() {

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
//        if (activity !is WeexActivity) {
//            return
//        }
        activity ?: return
        val dragLayout = activity.layoutInflater.inflate(R.layout.wx_debug_view, null) as DragLayout
        dragLayout.layoutParams = FrameLayout.LayoutParams(DimensUtils.dp2px(50f), DimensUtils.dp2px(50f))
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.leftMargin = 200
        params.bottomMargin = 20
        dragLayout.setOnClickListener {
            ToastUtils.show("click")
        }
        dragLayout.setOnLongClickListener {
            ToastUtils.show("long click")
            true
        }
        (activity.window.decorView as ViewGroup).addView(dragLayout, params)
    }
}