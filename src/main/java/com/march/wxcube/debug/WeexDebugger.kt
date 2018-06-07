package com.march.wxcube.debug

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.march.common.utils.ToastUtils
import com.march.wxcube.JsCacheStrategy
import com.march.wxcube.JsLoadStrategy
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.march.wxcube.common.click
import com.march.wxcube.common.newLine
import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDelegate

/**
 * CreateAt : 2018/5/3
 * Describe :
 *
 * @author chendong
 */
class WeexDebugger(private val mWeexDelegate: WeexDelegate,
                   private val mActivity: Activity,
                   private val mWeexPage: WeexPage?) : WeexLifeCycle {

    private var mLastTemplate: String? = null

    private val mHandler by lazy { Handler(Looper.getMainLooper()) { startRefresh() } }

    var isRefreshing = false

    private var mDebugBtn: TextView? = null

    private val mDebugDialog by lazy { DebugDialog(mActivity) }

    private val mDebugConfig by lazy { DebugConfig(false, false, false) }
    var mErrorMsg = ""

    fun addDebugBtn(container: ViewGroup) {
        if (mDebugBtn == null) {
            mDebugBtn = DragButton(container.context)
            mDebugBtn?.textSize = 12f
            mDebugBtn?.text = "debug"
            mDebugBtn.click {
                mDebugDialog.show()
            }
        }
        val params = FrameLayout.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.leftMargin = 200
        mDebugBtn?.gravity = Gravity.CENTER
        mDebugBtn?.setPadding(20, 5, 20, 5)
        mDebugBtn?.setBackgroundColor(Color.parseColor("#60e5df"))
        mDebugBtn?.setTextColor(Color.BLACK)
        container.addView(mDebugBtn, params)
    }

    private fun startRefresh(): Boolean {
        return with(mWeexDelegate) {
            isRefreshing = true
            var cacheStrategy = JsCacheStrategy.NO_CACHE
            if(mDebugConfig.debugJsInCache) {
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_ONLY
            } else if(mDebugConfig.debugJsInDisk){
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_DISK_BOTH
            }
            Weex.getInst().mWeexJsLoader.getTemplateAsync(mActivity,
                    JsLoadStrategy.NET_FIRST,cacheStrategy, mWeexPage) {
                it?.let {
                    if (mLastTemplate == null || !mLastTemplate.equals(it)) {
                        mActivity.runOnUiThread {
                            renderJs(it)
                            mLastTemplate = it
                        }
                    } else {
                        Weex.getInst().mWeexInjector.onLog("startRefresh", "获取到但是没有改变，不作渲染")
                    }
                }
                mHandler.sendEmptyMessageDelayed(0, 2000)
            }
            true
        }
    }

    private fun stopRefresh() {
        mLastTemplate = null
        mHandler.removeCallbacksAndMessages(null)
        isRefreshing = false
    }

    override fun onDestroy() {
        stopRefresh()
    }

    data class DebugConfig(var isDebugLocalJs: Boolean, var debugJsInCache: Boolean, var debugJsInDisk: Boolean)


    inner class DebugDialog(context: Context) : AppCompatDialog(context) {

        init {
            setContentView(R.layout.debug_dialog)
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            try {
                setDialogAttributes(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f, .6f, Gravity.CENTER)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val msg = StringBuilder().append("页面：").append(mWeexPage?.pageName).newLine()
                    .append("错误信息：").newLine()
                    .append(mErrorMsg)
                    .toString()
            findViewById<TextView>(R.id.desc_tv)?.text = msg
            findViewById<View>(R.id.req_config_btn).click { Weex.getInst().mWeexUpdater.update(context) }
            // 关闭
            findViewById<View>(R.id.close_btn).click { dismiss() }
            // 清理缓存的js
            findViewById<View>(R.id.clear_cache_btn).click { Weex.getInst().mWeexJsLoader.clearCache() }
            // 清理磁盘js`
            findViewById<View>(R.id.clear_disk_btn).click { Weex.getInst().clearDiskCache() }
            val jsInCacheSw = findViewById<Switch>(R.id.js_in_cache_sw)
            jsInCacheSw?.isChecked = mDebugConfig.debugJsInCache
            jsInCacheSw?.setOnCheckedChangeListener { _, isChecked ->
                mDebugConfig.debugJsInCache = isChecked
            }
            val jsInDiskSw = findViewById<Switch>(R.id.js_in_disk_sw)
            jsInDiskSw?.isChecked = mDebugConfig.debugJsInDisk
            jsInDiskSw?.setOnCheckedChangeListener { _, isChecked ->
                mDebugConfig.debugJsInDisk = isChecked
            }
            val debugJsSw = findViewById<Switch>(R.id.debug_local_js_sw)
            debugJsSw?.isChecked = mDebugConfig.isDebugLocalJs
            debugJsSw?.setOnCheckedChangeListener { _, isChecked ->
                mDebugConfig.isDebugLocalJs = isChecked
                if(mDebugConfig.isDebugLocalJs) {
                    stopRefresh()
                    startRefresh()
                    ToastUtils.show("开始调试远程js")
                } else {
                    stopRefresh()
                    ToastUtils.show("停止调试远程js")
                }
            }
        }

        /* 全部参数设置属性 */
        private fun setDialogAttributes(width: Int, height: Int, alpha: Float, dim: Float, gravity: Int) {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            val window = window ?: return
            val params = window.attributes
            // setContentView设置布局的透明度，0为透明，1为实际颜色,该透明度会使layout里的所有空间都有透明度，不仅仅是布局最底层的view
            params.alpha = alpha
            // 窗口的背景，0为透明，1为全黑
            params.dimAmount = dim
            params.width = width
            params.height = height
            params.gravity = gravity
            window.attributes = params
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        }
    }
}