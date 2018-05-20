package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.march.wxcube.Weex


/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
class IndexActivity : BaseActivity() {

    companion object {
        const val TIME_START = 1500L
    }

    private val mCtx by lazy { IndexActivity@ this }

    private val mIndexRunnable = Runnable {
        // 先打开一次，无法打开的话注册一个未来打开的 callback
        if (Weex.getInst().mWeexRouter.openIndexPage(mCtx)) {
            finish()
        } else {
            Weex.getInst().mWeexRouter.mRouterReadyCallback = {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.getInst().mWeexInjector.onPageCreated(this, Weex.PAGE_INDEX)
        Weex.getInst().mWeexInjector.getLoadingHandler().setIndexPageContent(this)
        // 1.5s 后启动
        Handler().postDelayed(mIndexRunnable, TIME_START)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        Weex.getInst().mWeexRouter.mRouterReadyCallback = null
    }
}
