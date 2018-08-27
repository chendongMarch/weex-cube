package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.march.wxcube.CubeWx
import com.march.wxcube.common.WxConstants
import com.march.wxcube.manager.ManagerRegistry
import java.lang.Exception


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
        if (CubeWx.mWxRouter.openIndexPage(mCtx)) {
            finish()
        } else {
            CubeWx.mWxRouter.mRouterReadyCallback = {
                CubeWx.mWxRouter.openIndexPage(mCtx)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            CubeWx.mWxPageAdapter.onPageCreated(this, WxConstants.PAGE_INDEX)
            CubeWx.mWxPageAdapter.getLoading()?.setIndexContent(this)
            CubeWx.mWxUpdater.update(this)
            ManagerRegistry.OnlineCfg.update(this)
            Handler().postDelayed(mIndexRunnable, TIME_START)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        CubeWx.mWxRouter.mRouterReadyCallback = null
    }

}
