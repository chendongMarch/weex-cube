package com.march.wxcube.debug

import android.app.Activity
import android.os.Bundle
import com.march.common.impl.ActivityLifecycleCallback
import com.march.wxcube.ui.WeexActivity

/**
 * CreateAt : 2018/6/15
 * Describe :
 *
 * @author chendong
 */
class WxDebugActivityLifeCycle : ActivityLifecycleCallback() {

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        if (activity !is WeexActivity) {
            return
        }
        val debugger = WeexPageDebugger()
        debugger.addDebugBtn(activity)
        activity.mDelegate.setDebugger(debugger)
    }

}