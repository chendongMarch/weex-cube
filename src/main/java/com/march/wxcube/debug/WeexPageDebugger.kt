package com.march.wxcube.debug

import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.march.common.utils.DimensUtils
import com.march.common.utils.RegexUtils
import com.march.common.utils.ToastUtils
import com.march.common.view.DragLayout
import com.march.wxcube.JsCacheStrategy
import com.march.wxcube.JsLoadStrategy
import com.march.wxcube.R
import com.march.wxcube.Weex
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

class WeexPageDebugger : IWXRenderListener {

    internal val mDebugMsg by lazy { DebugMsg() }
    private val mHandler by lazy { Handler(Looper.getMainLooper()) { startRefresh(false) } }
    private val mDebugDialog by lazy { PageDebugDialog(mActivity, this) }
    internal val mDebugConfig by lazy { DebugConfig(false, false, false) }

    private lateinit var mActivity: Activity
    private lateinit var mDelegate: WeexDelegate
    internal lateinit var mWeexPage: WeexPage

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

    internal fun startRefresh(once: Boolean): Boolean {
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
            Weex.mWeexJsLoader.getTemplateAsync(mActivity,
                    JsLoadStrategy.NET_FIRST,cacheStrategy, mWeexPage) {
                it?.let {
                    if (mDebugMsg.lastTemplate != it) {
                        mActivity.runOnUiThread {
                            ToastUtils.show("已为您刷新～")
                            renderJs(it)
                            mDebugMsg.lastTemplate = it
                        }
                    } else {
                        Weex.mWeexInjector.onLog("startRefresh", "获取到但是没有改变，不作渲染")
                    }
                }
                mHandler.post { mView?.animate()?.rotationYBy(360f)?.setDuration(5_00)?.start() }
                if (!once && mDebugMsg.refreshing && mDebugConfig.isRefreshRemoteJs) {
                    mHandler.sendEmptyMessageDelayed(0, 2000)
                }
            }
            true
        }
    }

    internal fun stopRefresh() {
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
                mDebugConfig.isRefreshRemoteJs = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
        mDebugMsg.errorMsg = "code = $errCode msg = $msg"
    }


    data class DebugConfig(var isRefreshRemoteJs: Boolean, var debugJsInCache: Boolean, var debugJsInDisk: Boolean)


}