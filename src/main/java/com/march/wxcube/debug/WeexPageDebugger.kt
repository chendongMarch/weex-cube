package com.march.wxcube.debug

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.TextView
import com.march.common.utils.DimensUtils
import com.march.common.utils.RegexUtils
import com.march.common.utils.ToastUtils
import com.march.common.view.DragLayout
import com.march.wxcube.JsCacheStrategy
import com.march.wxcube.JsLoadStrategy
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.march.wxcube.common.click
import com.march.wxcube.common.newLine
import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDelegate
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/5/3
 * Describe : 页面调试管理
 *
 * @author chendong
 */
class DebugMsg {
    var lastTemplate = ""
    var errorMsg = ""
    var refreshing = false

    fun toShowString(): String {
        return StringBuilder()
                .append("error = ").append(errorMsg).newLine()
                .append(if (refreshing) "刷新中" else "没有刷新").newLine()
                .toString()
    }
}

class WeexDebugger : IWXRenderListener {

    private val mDebugMsg by lazy { DebugMsg() }
    private val mHandler by lazy { Handler(Looper.getMainLooper()) { startRefresh(false) } }
    private val mDebugDialog by lazy { DebugDialog(mActivity) }
    private val mDebugConfig by lazy { DebugConfig(false, false, false) }

    private lateinit var mActivity: Activity
    private lateinit var mDelegate: WeexDelegate
    private lateinit var mWeexPage: WeexPage

    private var mIsDestroy = false
    private var mView: View? = null

    fun addDebugBtn(activity: Activity) {
        mActivity = activity
        val dragLayout = activity.layoutInflater.inflate(R.layout.wx_debug_view, null) as DragLayout
        dragLayout.layoutParams = FrameLayout.LayoutParams(DimensUtils.dp2px(50f), DimensUtils.dp2px(50f))
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.END or Gravity.BOTTOM
        params.rightMargin = 100
        params.bottomMargin = 300
        dragLayout.setOnClickListener {
            mDebugDialog.show()
        }
        dragLayout.setOnLongClickListener {
            stopRefresh()
            startRefresh(false)
            true
        }
        mView = dragLayout
        (activity.window.decorView as ViewGroup).addView(dragLayout, params)
    }

    private fun startRefresh(once: Boolean): Boolean {
        if (mIsDestroy) {
            return false
        }
        return with(mDelegate) {
            mDebugMsg.refreshing = true
            var cacheStrategy = JsCacheStrategy.NO_CACHE
            if(mDebugConfig.debugJsInCache) {
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_ONLY
            } else if(mDebugConfig.debugJsInDisk){
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_DISK_BOTH
            }
            Weex.getInst().mWeexJsLoader.getTemplateAsync(mActivity,
                    JsLoadStrategy.NET_FIRST,cacheStrategy, mWeexPage) {
                it?.let {
                    if (mDebugMsg.lastTemplate != it) {
                        mActivity.runOnUiThread {
                            ToastUtils.show("已为您刷新～")
                            renderJs(it)
                            mDebugMsg.lastTemplate = it
                        }
                    } else {
                        Weex.getInst().mWeexInjector.onLog("startRefresh", "获取到但是没有改变，不作渲染")
                    }
                }
                mHandler.post { mView?.animate()?.rotationYBy(360f)?.setDuration(5_00)?.start() }
                if (!once && mDebugMsg.refreshing) {
                    mHandler.sendEmptyMessageDelayed(0, 2000)
                }
            }
            true
        }
    }

    private fun stopRefresh() {
        mDebugMsg.lastTemplate = ""
        mDebugMsg.refreshing = false
        mHandler.removeCallbacksAndMessages(null)
    }

    fun onReady(delegate: WeexDelegate) {
        mDelegate = delegate
        mActivity = delegate.mActivity
        mWeexPage = mDelegate.mWeexPage
    }

    fun onDestroy() {
        stopRefresh()
        mIsDestroy = true
    }

    override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {

    }

    override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
    }

    override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
        if (mDebugMsg.refreshing || mIsDestroy) {
            return
        }
        try {
            val js = mWeexPage.remoteJs ?: return
            val uri = Uri.parse(js)
            if (RegexUtils.isIp(uri.host)) {
                ToastUtils.show("检测到调试地址，2s 后开始自动刷新")
                mHandler.sendEmptyMessageDelayed(0, 2000)
                mDebugMsg.refreshing = true
                mDebugConfig.isDebugLocalJs = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
        mDebugMsg.errorMsg = "code = $errCode msg = $msg"
    }


    data class DebugConfig(var isDebugLocalJs: Boolean, var debugJsInCache: Boolean, var debugJsInDisk: Boolean)


    inner class DebugDialog(context: Context) : AppCompatDialog(context) {

        init {
            setContentView(R.layout.debug_dialog)
        }

        private val hideBtn by lazy { findViewById<Button>(R.id.hide_btn) }
        private val descTv by lazy { findViewById<TextView>(R.id.desc_tv) }
        private val refreshJsSw by lazy { findViewById<Switch>(R.id.debug_local_js_sw) }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            try {
                setDialogAttributes(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f, .6f, Gravity.CENTER)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            hideBtn.click {
                if (descTv?.visibility == View.GONE) {
                    descTv?.visibility = View.VISIBLE
                    hideBtn?.text = "显示信息"
                } else {
                    descTv?.visibility = View.GONE
                    hideBtn?.text = "隐藏信息"
                }
            }
            updateMsg()
            findViewById<View>(R.id.info_btn).click {
                ToastUtils.showLong("""
                    1. 长按可以触发强制刷新
                    2. 当在实时刷新时，图标会保持旋转作为提示
                """.trimIndent())
            }
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
            refreshJsSw?.isChecked = mDebugConfig.isDebugLocalJs
            refreshJsSw?.setOnCheckedChangeListener { _, isChecked ->
                mDebugConfig.isDebugLocalJs = isChecked
                if(mDebugConfig.isDebugLocalJs) {
                    stopRefresh()
                    startRefresh(false)
                    ToastUtils.show("开始调试远程js")
                } else {
                    stopRefresh()
                    ToastUtils.show("停止调试远程js")
                }
            }
        }

        private fun updateMsg() {
            val msg = StringBuilder()
                    .append("页面：").newLine()
                    .append(mWeexPage.toShowString()).newLine()
                    .append("信息：").newLine()
                    .append(mDebugMsg.toShowString()).newLine()
                    .toString()
            descTv?.text = msg
        }

        override fun show() {
            super.show()
            updateMsg()
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