package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup

import com.march.wxcube.R
import com.march.wxcube.Weex


/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WeexActivity : WxBaseActivity() {

    val weexDelegate: WeexDelegate by lazy {
        WeexDelegate(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.getInst().mWeexInjector.onPageCreated(this, Weex.PAGE_WEEX)
        setContentView(R.layout.weex_activity)
        weexDelegate.render()
    }

    override fun onResume() {
        super.onResume()
        weexDelegate.onResume()
    }


    override fun onPause() {
        super.onPause()
        weexDelegate.onPause()

    }

    override fun onStart() {
        super.onStart()
        weexDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        weexDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        weexDelegate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        weexDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
