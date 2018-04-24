package com.march.wxcube.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.webkit.WebFragment
import com.march.wxcube.R

/**
 * CreateAt : 2018/4/17
 * Describe :
 *
 * @author chendong
 */
class WebActivity : AppCompatActivity() {

    private val mWebFragment by lazy {
        WebFragment.newInst(intent.extras)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionStatusBarUtils.setStatusBarLightMode(WebActivity@ this)
        setContentView(R.layout.web_activity)
        supportFragmentManager.beginTransaction()
                .add(R.id.web_activity_root, mWebFragment)
                .show(mWebFragment).commit()
    }

    override fun onBackPressed() {
        if (mWebFragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}