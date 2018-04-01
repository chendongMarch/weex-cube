package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup

import com.march.wxcube.R


/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WeexActivity : AppCompatActivity() {

    private var mContainerView: ViewGroup? = null

    val weexDelegate: WeexDelegate by lazy {
        WeexDelegate(this, object : WeexRender.RenderService {
            override fun onViewCreated(view: View) {
                mContainerView?.addView(view)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weex_activity)
        mContainerView = findViewById(R.id.weex_activity_root)
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
