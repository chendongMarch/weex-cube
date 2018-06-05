package com.march.wxcube.common

import android.content.Intent
import android.view.View
import com.march.wxcube.lifecycle.WeexLifeCycle
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/6/5
 * Describe :
 *
 * @author chendong
 */
class WxSDKInstLifeWrap(val inst: WXSDKInstance) : WeexLifeCycle {

    override fun onCreate() {
        inst.onActivityCreate()
    }

    override fun onViewCreated(view: View?) {
    }

    override fun onStart() {
        inst.onActivityStart()
    }

    override fun onResume() {
        inst.onActivityResume()
    }

    override fun onPause() {
        inst.onActivityPause()
    }

    override fun onStop() {
        inst.onActivityStop()
    }

    override fun onDestroy() {
        inst.onActivityDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        inst.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPermissionResult(requestCode: Int, resultCode: Int, data: Intent) {

    }
}