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

    override fun onActivityCreated(act: Activity?, savedInstanceState: Bundle?) {
        try {


//        if (act !is WeexActivity) {
//            return
//        }
            val activity = act ?: return
        val debugger = WeexPageDebugger()
        debugger.addDebugBtn(activity)
            if (activity is WeexActivity) {
                activity.mDelegate.setDebugger(debugger)
            }
        } catch (e: Exception) {

        }
    }

}