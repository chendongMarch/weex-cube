package com.march.wxcube.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.march.common.utils.LogUtils
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
    private val mIndexRunnable = object : Runnable {
        override fun run() {
            if (!Weex.getInst().mWeexRouter.openIndexPage(mCtx)) {
                LogUtils.e("检测一次，没有准备好")
                mHandler.postDelayed(this, 50)
            } else {
                mHandler.removeCallbacksAndMessages(null)
                finish()
                overridePendingTransition(0, 0)
            }
        }
    }

    val mHandler by lazy { Handler(Looper.getMainLooper()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.getInst().mWeexInjector.onPageCreated(this, Weex.PAGE_INDEX)
        Weex.getInst().mWeexInjector.getLoadingHandler().setIndexPageContent(this)
        // 2s 后启动
        mHandler.postDelayed(mIndexRunnable, TIME_START)
    }
}
