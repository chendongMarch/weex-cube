package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.march.common.utils.LogUtils
import com.march.webkit.IWebView
import com.march.webkit.x5.X5WebView
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.march.wxcube.common.report

import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.RenderContainer
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy
import java.lang.ref.WeakReference

import java.util.HashMap

/**
 * CreateAt : 2018/3/27
 * Describe : weex ui 代理
 *
 * @author chendong
 */
class WeexDelegate : WeexLifeCycle {

    companion object {
        val instanceDelegateMap: MutableMap<String, WeakReference<WeexDelegate>> = mutableMapOf()
    }

    private lateinit var mWeexRender: Render
    private lateinit var mWeexInst: WXSDKInstance
    private lateinit var mActivity: Activity

    var mFragmentLoader: FragmentLoader? = null
    private lateinit var mContainerView: ViewGroup // 容器 View
    private var mWeexView: ViewGroup? = null // weex root view
    private var mWeexPage: WeexPage? = null // 页面数据
    private val mHost: Any // 宿主
    private val mLoadingHandler by lazy { Weex.getInst().mWeexInjector.getLoadingHandler() }

    // 为 Fragment 提供构造方法
    constructor(fragment: Fragment) {
        this.mHost = fragment
        this.mWeexPage = fragment.arguments.getParcelable(WeexPage.KEY_PAGE)
        init(fragment.activity)
    }

    // 为 Activity 提供构造方法
    constructor(activity: Activity) {
        this.mHost = activity
        initContainerView(activity.findViewById(R.id.weex_activity_root))
        this.mWeexPage = activity.intent.getParcelableExtra(WeexPage.KEY_PAGE)
        init(activity)
    }

    // 初始化方法
    private fun init(activity: Activity) {
        this.mActivity = activity
        this.mWeexInst = WXSDKInstance(mActivity)
        this.mWeexRender = Render(mActivity, mWeexInst, RenderListener())
        instanceDelegateMap[mWeexInst.instanceId] = WeakReference(this)
    }


    fun render() {
        val page = mWeexPage ?: return
        val realUrl = page.webUrl ?: return
        val opts = HashMap<String, Any>()
        // parse url
        if (!realUrl.isEmpty()) {
            val uri = Uri.parse(realUrl)
            val parameterNames = uri.queryParameterNames
            for (name in parameterNames) {
                opts[name] = uri.getQueryParameter(name)
            }
        }
        // parse data
        val data = ManagerRegistry.DATA.getData(realUrl)
        if (data != null) {
            opts["extraData"] = data
        }
        mWeexRender.render(page, opts)
    }

    fun initContainerView(view: ViewGroup) {
        mContainerView = view
        mLoadingHandler.addLoadingView(mContainerView)
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
        ManagerRegistry.getInst().onWxInstRelease(mWeexPage, mWeexInst)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        mWeexInst.onActivityResult(requestCode, resultCode, data)
    }

    private var iWebView: IWebView? = null

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
            LogUtils.e("onViewCreated")
        }

        override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
            report("code = $errCode, msg = $msg")
            if (mWeexPage?.webUrl != null) {
                if (iWebView == null) {
                    iWebView = X5WebView(mActivity)
                }
                mContainerView.removeAllViews()
                mContainerView.addView(iWebView as View)
                iWebView?.loadPage(mWeexPage?.webUrl)
            }
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
        }
    }


    class Render(activity: Activity,
                 private val mWxInst: WXSDKInstance,
                 private val listener: IWXRenderListener) {

        init {
            val renderContainer = RenderContainer(activity)
            mWxInst.setRenderContainer(renderContainer)
            mWxInst.registerRenderListener(listener)
        }

        fun render(page: WeexPage, opts: Map<String, Any>) {
            if (!page.isValid) {
                listener.onException(mWxInst, "100", "页面数据有问题")
                return
            }
            Weex.getInst().mWeexJsLoader.getTemplateAsync(mWxInst.context, page) {
                if (!it.isNullOrBlank()) {
                    mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
                } else if (!page.remoteJs.isNullOrBlank()) {
                    mWxInst.renderByUrl(page.pageName, page.remoteJs, opts, null, WXRenderStrategy.APPEND_ASYNC)
                } else {
                    report("render error " + page.toString())
                    listener.onException(mWxInst, "101", "js 没有准备好")
                }
            }
        }


    }
}
