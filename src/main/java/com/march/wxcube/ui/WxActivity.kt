package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle

import com.march.wxcube.CubeWx


/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WxActivity : BaseActivity() {

    val mLoadingIndicator by lazy { CubeWx.mWxPageAdapter.getLoading().makeLoadingIndicator(this) }
    val mDelegate: WxDelegate by lazy { WxDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CubeWx.mWxPageAdapter.onPageCreated(this, CubeWx.PAGE_WEEX)
        mDelegate.onCreate()
        mDelegate.render()
    }

    override fun onResume() {
        super.onResume()
        mDelegate.onResume()
    }

    override fun onPause() {
        super.onPause()
        mDelegate.onPause()
    }

    override fun onStart() {
        super.onStart()
        mDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        mDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelegate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mDelegate.onActivityResult(requestCode, resultCode, data)
    }


    override fun onBackPressed() {
        mDelegate.onBackPressed()
        if (mDelegate.mInterceptBackPressed) {
            return
        }
        super.onBackPressed()
    }
}
