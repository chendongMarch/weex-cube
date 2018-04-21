package com.march.wxcube.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.webkit.IWebView
import com.march.webkit.sys.SysWebView
import com.march.webkit.x5.X5WebView
import com.march.wxcube.manager.ManagerRegistry

/**
 * CreateAt : 2018/4/17
 * Describe :
 *
 * @author chendong
 */
class WebActivity : AppCompatActivity() {

    companion object {
        const val KEY_URL = "key-url"
    }

    private val iWebView: IWebView by lazy {
        SysWebView(WebActivity@ this)
//      X5WebView(WebActivity@ this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionStatusBarUtils.setStatusBarLightMode(WebActivity@ this)
        setContentView(iWebView as View)
        val url = intent.getStringExtra(KEY_URL)
        val safeUrl = ManagerRegistry.ENV.safeUrl(url)
        iWebView.loadPage(safeUrl)
    }

    override fun onBackPressed() {
        if (iWebView.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}