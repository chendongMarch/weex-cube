package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle

import com.march.wxcube.CubeWx
import com.march.wxcube.R
import com.march.wxcube.common.WxConstants


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
        CubeWx.mWxPageAdapter.onPageCreated(this, WxConstants.PAGE_WEEX)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

    override fun finish() {
        super.finish()
        val anim = intent.getStringExtra("animation")
        when (anim) {
            "btc"  -> overridePendingTransition(R.anim.act_no_anim, R.anim.act_bottom_out)
            "fade" -> overridePendingTransition(R.anim.act_no_anim, R.anim.act_fast_fade_out)
            "rtl"  -> overridePendingTransition(R.anim.act_no_anim, R.anim.act_translate_out)
            "no"  -> overridePendingTransition(R.anim.act_no_anim, R.anim.act_no_anim)
            else   -> overridePendingTransition(R.anim.act_no_anim, R.anim.act_translate_out)
        }
    }
}
