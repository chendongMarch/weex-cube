package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.march.common.utils.LogUtils
import com.march.webkit.IWebView
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.march.wxcube.common.report
import com.march.wxcube.debug.WeexDebugger
import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.WXSDKInstance
import java.util.*

/**
 * CreateAt : 2018/3/27
 * Describe : weex ui 代理
 *
 * @author chendong
 */
class WeexDelegate : WeexLifeCycle {

    // weex 实例
    private lateinit var mWeexInst: WXSDKInstance
    // 渲染
    private lateinit var mWeexRender: WeexRender
    // 宿主
    private lateinit var mActivity: Activity
    private val mHost: Any
    // 负责加载多个 fragment
    var mFragmentLoader: FragmentLoader? = null
    var mWeexDebugger: WeexDebugger? = null
    // 容器
    private lateinit var mContainerView: ViewGroup // 容器 View
    private var mWeexView: ViewGroup? = null // weex root view
    // 页面数据
    private var mWeexPage: WeexPage
    // loading
    private val mLoadingHandler by lazy { Weex.getInst().mWeexInjector.getLoadingHandler() }
    // 降级 webview
    private var iWebView: IWebView? = null

    // 为 Fragment 提供构造方法
    constructor(fragment: Fragment) {
        mHost = fragment
        mWeexPage = fragment.arguments.getParcelable(WeexPage.KEY_PAGE)
        init(fragment.activity)
    }

    // 为 Activity 提供构造方法
    constructor(activity: Activity) {
        mHost = activity
        mWeexPage = activity.intent.getParcelableExtra(WeexPage.KEY_PAGE)
        init(activity)
        initContainerView(activity.findViewById(R.id.weex_root))
    }

    // 初始化方法
    fun init(activity: Activity) {
        mActivity = activity
        mWeexInst = WXSDKInstance(mActivity)
        mWeexRender = WeexRender(mActivity, mWeexInst, RenderListener())
        ManagerRegistry.getInst().onWxInstInit(mWeexPage, mWeexInst, this)
    }

    private fun parseRenderOptions(): Map<String, Any> {
        val opts = HashMap<String, Any>()
        // parse url
        val uri = Uri.parse(mWeexPage.webUrl)
        uri.queryParameterNames.forEach { opts[it] = uri.getQueryParameter(it) }
        mWeexPage.webUrl?.let {
            val data = ManagerRegistry.DATA.getData(it)
            if (data != null) {
                opts["extraData"] = data
            }
        }
        return opts
    }

    fun render() {
        mWeexRender.render(mWeexPage, parseRenderOptions())
    }

    private var mCurPage: WeexPage? = null

    fun renderError() {
        val errPage = Weex.getInst().mWeexRouter.findPage("/status/not-found-weex") ?: return
        mCurPage = errPage
        mWeexRender.render(errPage, parseRenderOptions())
    }

    fun renderJs(js: String) {
        mCurPage = mWeexPage
        mWeexRender.renderJs(mWeexPage, parseRenderOptions(), js)
    }

    fun initContainerView(view: ViewGroup) {
        mContainerView = view
        mLoadingHandler.addLoadingView(mContainerView)
        mWeexDebugger = WeexDebugger(this, mActivity, mWeexPage)
    }


    override fun close() {
        when (mHost) {
            is WeexActivity -> mHost.finish()
            is WeexFragment -> mHost.activity.finish()
            is WeexDialogFragment -> mHost.dismiss()
        }
    }

    override fun onCreate() {
        mWeexInst.onActivityCreate()
    }

    override fun onStart() {
        mWeexInst.onActivityStart()
    }

    override fun onResume() {
        mWeexInst.onActivityResume()
    }

    override fun onPause() {
        mWeexInst.onActivityPause()
    }

    override fun onStop() {
        mWeexInst.onActivityStop()
    }

    override fun onDestroy() {
        mWeexInst.onActivityDestroy()
        mWeexInst.registerRenderListener(null)
        ManagerRegistry.getInst().onWxInstRelease(mWeexPage, mWeexInst)
        mWeexDebugger?.onDestroy()
    }

    fun onDebugDestroy() {
        mWeexInst.onActivityDestroy()
        mWeexInst.registerRenderListener(null)
        ManagerRegistry.getInst().onWxInstRelease(mWeexPage, mWeexInst)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        mWeexInst.onActivityResult(requestCode, resultCode, data)
    }


    inner class RenderListener : IWXRenderListener {
        override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            mFragmentLoader?.onViewCreated()
            LogUtils.e("onRenderSuccess")
            mLoadingHandler.finish(mContainerView)
        }

        override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
            mWeexView = view as ViewGroup
            mContainerView.removeAllViews()
            mContainerView.addView(view, 0)
            mWeexDebugger?.addDebugBtn(mContainerView)
            LogUtils.e("onViewCreated")
        }

        override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
            report("code = $errCode, msg = $msg")
            mWeexDebugger?.mErrorMsg = "code = $errCode, msg = $msg"
            if (mWeexDebugger != null && mWeexDebugger?.isRefreshing != null && mWeexDebugger?.isRefreshing!!) {
                report("调试模式js出错，改正后会重新渲染")
                return
            }

            if(mCurPage == null || mCurPage?.equals(mWeexPage) == true) {
                renderError()
            }

//            if (mWeexPage.webUrl != null) {
//                if (iWebView == null) {
//                    iWebView = SysWebView(mActivity)
//                }
//                mContainerView.removeAllViews()
//                mContainerView.addView(iWebView as View)
//                iWebView?.loadPage(mWeexPage.webUrl)
//            }
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
        }
    }
}
