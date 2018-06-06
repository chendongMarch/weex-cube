package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle

import com.march.wxcube.R
import com.march.wxcube.Weex


/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WeexActivity : BaseActivity() {

    val mDelegate: WeexDelegate by lazy {
        WeexDelegate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.getInst().mWeexInjector.onPageCreated(this, Weex.PAGE_WEEX)
//        setContentView(R.layout.weex_container)
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
}
