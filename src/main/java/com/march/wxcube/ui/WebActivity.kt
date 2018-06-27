package com.march.wxcube.ui

import android.os.Bundle
import com.march.webkit.WebFragment
import com.march.wxcube.R
import com.march.wxcube.Weex

/**
 * CreateAt : 2018/4/17
 * Describe :
 *
 * @author chendong
 */
class WebActivity : BaseActivity() {

    private val mWebFragment by lazy {
        WebFragment.newInst(intent.extras)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.mWxPageAdapter.onPageCreated(this, Weex.PAGE_WEB)
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