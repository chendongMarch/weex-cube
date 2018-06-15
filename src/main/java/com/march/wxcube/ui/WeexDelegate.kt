package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.march.common.utils.LgUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.Weex
import com.march.wxcube.common.report
import com.march.wxcube.debug.WeexDebugger
import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.march.wxcube.performer.IPerformer
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

    companion object {
        const val EXTRA = "extra"
    }

    // weex 实例
    private lateinit var mWeexInst: WXSDKInstance
    // 渲染
    private lateinit var mWeexRender: WeexRender
    // 宿主
    private lateinit var mActivity: Activity
    private val mHost: Any
    // 负责加载多个 fragment
    var mWeexDebugger: WeexDebugger? = null
    // 容器
    lateinit var mContainerView: ViewGroup // 容器 View
    private var mWeexView: ViewGroup? = null // weex root view
    // 页面数据
    private lateinit var mWeexPage: WeexPage
    // loading
    private val mLoadingHandler by lazy { Weex.getInst().mWeexInjector.getLoading() }
    private var mIsRenderSuccess = false
    private var mCurPage: WeexPage? = null
    private val mPerformers by lazy { mutableMapOf<String, IPerformer>() }
    private val mLifeCallbacks by lazy { mutableListOf<WeexLifeCycle>() }

    /**
     * 为 Fragment 提供构造方法
     */
    constructor(fragment: Fragment) {
        mHost = fragment
        mWeexPage = fragment.arguments?.getParcelable(WeexPage.KEY_PAGE) ?: return
        val act = fragment.activity ?: return
        init(act)
    }

    /**
     * 为 Activity 提供构造方法
     */
    constructor(activity: Activity) {
        mHost = activity
        mWeexPage = activity.intent.getParcelableExtra(WeexPage.KEY_PAGE)
        init(activity)
        initContainerView(activity.findViewById(android.R.id.content))
    }

    /**
     * 初始化方法
     */
    private fun init(activity: Activity) {
        mActivity = activity
        createWxInst()
    }

    fun addPerformer(performer: IPerformer) {
        mPerformers[performer.javaClass.simpleName] = performer
        mLifeCallbacks.add(performer)
    }

    fun <T> getPerformer(clazz: Class<T>): T? {
        val performer = mPerformers[clazz.simpleName]
        return if (performer == null) {
            null
        } else {
            performer as? T
        }
    }

    /**
     * 销毁 weex 实例
     */
    private fun destroyWxInst() {
        mWeexInst.onActivityDestroy()
        mWeexRender.onDestroy()
        ManagerRegistry.getInst().onWxInstRelease(mWeexPage, mWeexInst)
        mLifeCallbacks.forEach { it.onDestroy() }
        mLifeCallbacks.clear()
    }

    /**
     * 创建 weex 实例
     */
    private fun createWxInst() {
        mWeexInst = WXSDKInstance(mActivity)
        mWeexRender = WeexRender(mActivity, mWeexInst, RenderListener())
        ManagerRegistry.getInst().onWxInstInit(mWeexPage, mWeexInst, this)
    }


    fun initContainerView(view: ViewGroup) {
        mContainerView = view
        mLoadingHandler.startWeexLoading(mContainerView)
        mWeexDebugger = WeexDebugger(this, mActivity, mWeexPage)
    }


    fun close() {
        when (mHost) {
            is WeexActivity -> mHost.finish()
            is WeexFragment -> mHost.activity?.finish()
            is WeexDialogFragment -> mHost.dismiss()
        }
    }

    inner class RenderListener : IWXRenderListener {
        override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            LgUtils.e("onRenderSuccess")
            mIsRenderSuccess = true
            mLoadingHandler.finishWeexLoading(mContainerView)
        }

        override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
            this@WeexDelegate.onViewCreated(view)
        }

        override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
            report("code = $errCode, msg = $msg")
            mWeexDebugger?.mErrorMsg = "code = $errCode, msg = $msg"
            if (mWeexDebugger != null && mWeexDebugger?.isRefreshing != null && mWeexDebugger?.isRefreshing!!) {
                report("调试模式js出错，改正后会重新渲染")
                ToastUtils.showLong(mWeexDebugger?.mErrorMsg)
                return
            }
            if (mIsRenderSuccess) {
                return
            }
            if (mCurPage == null || mCurPage?.equals(mWeexPage) == true) {
                renderNotFound()
            }
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
        }
    }

    //************************渲染页面*********************//


    /**
     * 准备渲染的参数
     */
    private fun parseRenderOptions(): Map<String, Any> {
        val opts = HashMap<String, Any>()
        // parse url
        val uri = Uri.parse(mWeexPage.webUrl)
        uri.queryParameterNames.forEach { opts[it] = uri.getQueryParameter(it) }
        opts["instanceId"] = mWeexInst.instanceId
        mWeexPage.webUrl?.let {
            val data = ManagerRegistry.DATA.getData(it)
            if (data != null) {
                opts[EXTRA] = data
            }
        }
        return opts
    }

    /**
     * 渲染之前处理
     */
    private fun preRender() {
        if (mIsRenderSuccess) {
            destroyWxInst()
            createWxInst()
            onCreate()
        }
    }

    /**
     * 渲染页面
     */
    private fun render(page: WeexPage) {
        preRender()
        mCurPage = page
        mWeexRender.render(page, parseRenderOptions())
    }

    /**
     * 渲染当前页面
     */
    fun render() {
        render(mWeexPage)
    }

    /**
     * 渲染指定的js，参数还是用本页面参数
     */
    fun renderJs(js: String) {
        preRender()
        mCurPage = mWeexPage
        mWeexRender.renderJs(mWeexPage, parseRenderOptions(), js)
    }

    /**
     * 渲染 not found 页面
     */
    fun renderNotFound() {
        val errPage = Weex.getInst().mWeexRouter.findPage("/status/not-found-weex") ?: return
        render(errPage)
    }

    //************************同步生命周期函数*********************//

    private fun fireEvent(event: String, data: Map<String,Any> = mapOf()) {
        mWeexInst.fireGlobalEventCallback(event, data)
    }

    override fun onDestroy() {
        destroyWxInst()
        mWeexDebugger?.onDestroy()
    }

    override fun onViewCreated(view: View?) {
        super.onViewCreated(view)
        LgUtils.e("onViewCreated")
        mWeexView = view as ViewGroup
        mContainerView.removeAllViews()
        mContainerView.addView(view, 0)
        mWeexDebugger?.addDebugBtn(mContainerView)
        mLifeCallbacks.forEach { it.onViewCreated(view) }
    }

    override fun onCreate() {
        mWeexInst.onActivityCreate()
        mLifeCallbacks.forEach { it.onCreate() }
    }

    override fun onStart() {
        mWeexInst.onActivityStart()
        mLifeCallbacks.forEach { it.onStart() }
    }

    override fun onResume() {
        mWeexInst.onActivityResume()
        mLifeCallbacks.forEach { it.onResume() }
        fireEvent("resume")
    }

    override fun onPause() {
        mWeexInst.onActivityPause()
        mLifeCallbacks.forEach { it.onPause() }
    }

    override fun onStop() {
        mWeexInst.onActivityStop()
        mLifeCallbacks.forEach { it.onStop() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        mWeexInst.onActivityResult(requestCode, resultCode, data)
        mLifeCallbacks.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onPermissionResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onPermissionResult(requestCode, resultCode, data)
        mLifeCallbacks.forEach { it.onPermissionResult(requestCode, resultCode, data) }
    }

    var mHandleBackPressed = false
    fun onBackPressed() {
        fireEvent("onBackPressed")
    }
}
