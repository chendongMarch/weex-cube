package com.march.wxcube.debug

import android.app.Activity
import android.os.Bundle
import com.march.common.impl.ActivityLifecycleCallback
import com.march.wxcube.ui.WxActivity

/**
 * CreateAt : 2018/6/15
 * Describe :
 *
 * @author chendong
 */
class WxDebugActivityLifeCycle : ActivityLifecycleCallback() {

    override fun onActivityCreated(act: Activity?, savedInstanceState: Bundle?) {
        try {
//        if (act !is WeexActivity) {
//            return
//        }
        val activity = act ?: return
        val debugger = WxPageDebugger()
        debugger.addDebugBtn(activity)
            if (activity is WxActivity) {
                activity.mDelegate.setDebugger(debugger)
            }
        } catch (e: Exception) {

        }
    }

}