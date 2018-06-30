package com.march.wxcube.debug

import android.animation.Animator
import android.app.Activity
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.march.common.impl.AnimatorListener
import com.march.common.utils.DimensUtils
import com.march.common.utils.RegexUtils
import com.march.common.utils.ToastUtils
import com.march.common.view.DragLayout
import com.march.wxcube.CubeWx
import com.march.wxcube.R
import com.march.wxcube.common.WxUtils
import com.march.wxcube.lifecycle.WxLifeCycle
import com.march.wxcube.loader.JsCacheStrategy
import com.march.wxcube.loader.JsLoadStrategy
import com.march.wxcube.model.WxPage
import com.march.wxcube.ui.WxDelegate
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/5/3
 * Describe : 页面调试管理
 *
 * @author chendong
 */
class WxPageDebugger : IWXRenderListener, WxLifeCycle {

    companion object {
        const val MAX_REFRESH_COUNT = 30
    }

    private var mRefreshCount = 0
    internal val mDebugMsg by lazy { DebugMsg() }
    private val mHandler by lazy { Handler(Looper.getMainLooper()) { startRefresh(false,false) } }
    private val mDebugDialog by lazy { PageDebugDialog(mActivity, this) }
    internal val mDebugConfig by lazy { DebugConfig(false, false, false) }

    private lateinit var mActivity: Activity
    private var mDelegate: WxDelegate? = null
    internal var mWeexPage: WxPage? = null

    private var mIsDestroy = false
    private var mView: View? = null
    private var mVibrator: Vibrator? = null


    fun addDebugBtn(activity: Activity) {
        mVibrator = activity.getSystemService(Activity.VIBRATOR_SERVICE) as Vibrator
        mActivity = activity
        val dragLayout = activity.layoutInflater.inflate(R.layout.wx_debug_view, null) as DragLayout
        dragLayout.layoutParams = FrameLayout.LayoutParams(DimensUtils.dp2px(50f), DimensUtils.dp2px(50f))
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.END or Gravity.BOTTOM
        params.rightMargin = 100
        params.bottomMargin = 330
        dragLayout.setOnClickListener {
            mDebugDialog.show()
        }
        dragLayout.setOnLongClickListener {
            mVibrator?.vibrate(300)
            mDebugConfig.isRefreshRemoteJs = true
            stopRefresh()
            startRefresh(false)
            true
        }
        mView = dragLayout
        (activity.window.decorView as ViewGroup).addView(dragLayout, params)
    }

    internal fun startRefresh(once: Boolean, isStartUp: Boolean = true): Boolean {
        if (mIsDestroy) {
            return false
        }
        val delegate = mDelegate ?: return false
        if (isStartUp) {
            ToastUtils.show("开始刷新~")
            mRefreshCount = 0
        }
        if(mRefreshCount > MAX_REFRESH_COUNT) {
            ToastUtils.show("自动刷新${MAX_REFRESH_COUNT}次，没有改变，暂停刷新，可以长按调试按钮重新开启")
            stopRefresh()
            return false
        }
        return with(delegate) {
            mDebugMsg.refreshing = true
            var cacheStrategy = JsCacheStrategy.NO_CACHE
            if(mDebugConfig.debugJsInCache) {
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_ONLY
            } else if(mDebugConfig.debugJsInDisk){
                cacheStrategy = JsCacheStrategy.CACHE_MEMORY_DISK_BOTH
            }
            CubeWx.mWxJsLoader.getTemplateAsync(mActivity,
                    JsLoadStrategy.NET_FIRST,cacheStrategy, mWeexPage) {
                it?.let {
                    if (mDebugMsg.lastTemplate != it) {
                        if (mDebugMsg.lastTemplate.isBlank()) {
                            mDebugMsg.lastTemplate = it
                        } else {
                            mActivity.runOnUiThread {
                                renderJs(it)
                                mDebugMsg.lastTemplate = it
                                ToastUtils.show("已为您刷新～")
                            }
                        }
                        mRefreshCount = 0
                    } else {
                        mRefreshCount++
                        CubeWx.mWxReportAdapter.log("startRefresh", "获取到但是没有改变，不作渲染")
                    }
                }
                mHandler.post {
                    mView?.animate()
                            ?.rotationYBy(360f)
                            ?.setDuration(5_00)
                            ?.setListener(object : AnimatorListener() {
                                override fun onAnimationEnd(animation: Animator?) {
                                    mView?.rotationY = 0f
                                }
                            })
                            ?.start()
                }
                if (!once && mDebugMsg.refreshing && mDebugConfig.isRefreshRemoteJs) {
                    mHandler.sendEmptyMessageDelayed(0, 5000)
                }
            }
            true
        }
    }

    internal fun stopRefresh(immediately:Boolean = true) {
        mDebugMsg.lastTemplate = if(immediately) "stop" else ""
        mDebugMsg.refreshing = false
        mHandler.removeCallbacksAndMessages(null)
    }

    fun onReady(delegate: WxDelegate) {
        mDelegate = delegate
        mActivity = delegate.mActivity
        mWeexPage = delegate.mWeexPage
    }

    override fun onDestroy() {
        mVibrator?.cancel()
        mVibrator = null
        stopRefresh(false)
        mIsDestroy = true
    }

    override fun onPause() {
        super.onPause()
        if (mDebugConfig.isRefreshRemoteJs) {
            stopRefresh(false)
            // ToastUtils.show("页面${mWeexPage?.pageName}进入后台，暂停轮询")
        }
    }

    override fun onResume() {
        super.onResume()
        if (mDebugConfig.isRefreshRemoteJs) {
            startRefresh(false)
            // ToastUtils.show("页面${mWeexPage?.pageName}进入后台，开始轮询")
        }
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
            val jsUrl = mWeexPage?.remoteJs ?: return
            val js = WxUtils.rewriteUrl(jsUrl, URIAdapter.BUNDLE)
            val uri = Uri.parse(js)
            if (RegexUtils.isIp(uri.host)) {
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