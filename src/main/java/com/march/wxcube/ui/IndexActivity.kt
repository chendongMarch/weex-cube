package com.march.wxcube.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.march.common.utils.ToastUtils
import com.march.wxcube.Weex
import com.march.wxcube.common.Permission


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
        Weex.getInst().mWeexInjector.getLoading().setIndexContent(this)
        checkPermissionAndLaunch()
    }

    private fun checkPermissionAndLaunch() {
        val result = Permission.checkPermission(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE))
        if (result) {
            // 1.5s 后启动
            Weex.getInst().mWeexUpdater.update(this)
            Handler().postDelayed(mIndexRunnable, TIME_START)
        } else {
            ToastUtils.show("请授予达人店存储权限～")
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        Weex.getInst().mWeexRouter.mRouterReadyCallback = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissionAndLaunch()
    }
}
