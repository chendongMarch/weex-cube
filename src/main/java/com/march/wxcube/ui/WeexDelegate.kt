package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.march.common.utils.LgUtils
import com.march.wxcube.Weex
import com.march.wxcube.common.report
import com.march.wxcube.debug.WeexPageDebugger
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
        const val INSTANCE_ID = "instanceId"
    }

    // 渲染状态
    private var mRenderStatus = RenderStatus.RENDER_NONE
    // weex 实例
    private lateinit var mWeexInst: WXSDKInstance
    // 渲染
    private lateinit var mWeexRender: WeexRender
    // 宿主
    internal lateinit var mActivity: Activity
    private val mHost: Any
    // 容器
    internal lateinit var mContainerView: ViewGroup // 容器 View
    // loading
    private val mLoadingHandler by lazy { Weex.mWeexInjector.getLoading() }
    // 当前加载的页面
    private var mCurPage: WeexPage? = null
    // 当前承载的页面
    internal lateinit var mWeexPage: WeexPage
    private var mWeexDebugger: WeexPageDebugger? = null
    // 附加数据和操作
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

    fun initContainerView(view: ViewGroup) {
        mContainerView = view
        mLoadingHandler.startWeexLoading(mContainerView)
    }

    fun addPerformer(performer: IPerformer) {
        mPerformers[performer.javaClass.simpleName] = performer
        mLifeCallbacks.add(performer)
    }

    fun addLifeCallbacks(callback: WeexLifeCycle) {
        mLifeCallbacks.add(callback)
    }

    fun setDebugger(weexDebugger: WeexPageDebugger) {
        mWeexDebugger = weexDebugger
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
            mRenderStatus = RenderStatus.RENDER_SUCCESS
            mLoadingHandler.finishWeexLoading(mContainerView)
            mWeexDebugger?.onRenderSuccess(instance, width, height)
        }

        override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
            this@WeexDelegate.onViewCreated(view)
        }

        override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
            report("code = $errCode, msg = $msg")
            mWeexDebugger?.onException(instance, errCode, msg)
            // 正在 js 刷新时直接跳过后续异常处理
            if (mWeexDebugger != null) {
                return
            }
            // 如果已经成功过，则此时不会走失败页面，只会没有反应
            if (mRenderStatus == RenderStatus.RENDER_SUCCESS) {
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
        opts[INSTANCE_ID] = mWeexInst.instanceId
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
        if (mRenderStatus == RenderStatus.RENDER_SUCCESS) {
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
        mRenderStatus = RenderStatus.RENDER_DOING
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
        val errPage = Weex.mWeexRouter.findPage("/status/not-found-weex") ?: return
        render(errPage)
    }

    //************************同步生命周期函数*********************//

    private fun fireEvent(event: String, data: Map<String,Any> = mapOf()) {
        mWeexInst.fireGlobalEventCallback(event, data)
    }

    override fun onDestroy() {
        destroyWxInst()
        mLifeCallbacks.forEach { it.onDestroy() }
        mWeexDebugger?.onDestroy()
    }

    override fun onViewCreated(view: View?) {
        super.onViewCreated(view)
        LgUtils.e("onViewCreated")
        mContainerView.removeAllViews()
        mContainerView.addView(view, 0)
        mLifeCallbacks.forEach { it.onViewCreated(view) }
    }

    override fun onCreate() {
        mWeexInst.onActivityCreate()
        mLifeCallbacks.forEach { it.onCreate() }
        mWeexDebugger?.onReady(this)
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


enum class RenderStatus(val value: Int) {
    RENDER_NONE(1),
    RENDER_DOING(2),
    RENDER_SUCCESS(3),
    RENDER_FAILURE(4),
}