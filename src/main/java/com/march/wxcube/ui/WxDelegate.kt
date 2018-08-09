package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import com.march.common.extensions.BarUI
import com.march.wxcube.CubeWx
import com.march.wxcube.adapter.IWxReportAdapter
import com.march.wxcube.common.WxUtils
import com.march.wxcube.debug.WxPageDebugger
import com.march.wxcube.lifecycle.WxLifeCycle
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WxPage
import com.march.wxcube.performer.IPerformer
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/3/27
 * Describe : weex ui 代理
 *
 * @author chendong
 */
class WxDelegate : WxLifeCycle {

    companion object {
        const val EXTRA = "extra"
        const val INSTANCE_ID = "instanceId"
        const val TOP_SAFE_AREA_HEIGHT = "topSafeAreaHeight"
        const val BOTTOM_SAFE_AREA_HEIGHT = "bottomSafeAreaHeight"
        const val BUNDLE_URL = "bundleUrl"
        const val VIRTUAL_BAR_HEIGHT = "virtualBarHeight"
        const val H5_URL = "h5Url"

        var virtualHeight = -1
    }

    // 渲染状态
    private var mRenderStatus = RenderStatus.RENDER_NONE
    // weex 实例
    private lateinit var mWeexInst: WXSDKInstance
    // 渲染
    private lateinit var mWeexRender: WxRender
    // 宿主
    internal lateinit var mActivity: Activity
    private val mHost: Any
    // 容器
    internal lateinit var mContainerView: ViewGroup // 容器 View
    // loading
    private val mLoadingHandler by lazy { CubeWx.mWxPageAdapter.getLoading() }
    // 当前加载的页面
    private var mCurPage: WxPage? = null
    // 当前承载的页面
    lateinit var mWxPage: WxPage
    private var mWeexDebugger: WxPageDebugger? = null
    // 附加数据和操作
    private val mPerformers by lazy { mutableMapOf<String, IPerformer>() }
    private val mLifeCallbacks by lazy { mutableListOf<WxLifeCycle>() }
    private val needImmersion by lazy {
        CubeWx.mWxCfg.allImmersion || CubeWx.mWxPageAdapter.getImmersionPages().contains(mWxPage.pageName)
    }


    /**
     * 为 Fragment 提供构造方法
     */
    constructor(fragment: Fragment) {
        mHost = fragment
        mWxPage = fragment.arguments?.getParcelable(WxPage.KEY_PAGE) ?: WxPage.errorPage() ?: return
        val act = fragment.activity ?: return
        init(act)
    }

    /**
     * 为 Activity 提供构造方法
     */
    constructor(activity: Activity) {
        mHost = activity
        mWxPage = activity.intent.getParcelableExtra(WxPage.KEY_PAGE) ?: WxPage.errorPage() ?: return
        init(activity)
        initContainerView(activity.findViewById(android.R.id.content))
    }

    /**
     * 初始化方法
     */
    private fun init(activity: Activity) {
        mActivity = activity
        if (needImmersion) {
            // BarUI.translucent(mActivity)
        }
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

    fun addLifeCallbacks(callback: WxLifeCycle) {
        mLifeCallbacks.add(callback)
    }

    fun setDebugger(weexDebugger: WxPageDebugger) {
        mWeexDebugger = weexDebugger
    }

    fun <T> getPerformer(clazz: Class<T>): T? {
        val performer = mPerformers[clazz.simpleName]
        return if (performer == null) {
            null
        } else {
            @Suppress("UNCHECKED_CAST")
            performer as? T
        }
    }

    /**
     * 销毁 weex 实例
     */
    private fun destroyWxInst() {
        mWeexInst.onActivityDestroy()
        mWeexRender.onDestroy()
        ManagerRegistry.getInst().onWxInstRelease(mWxPage, mWeexInst)
        mLifeCallbacks.forEach { it.onDestroy() }
        mLifeCallbacks.clear()
    }

    /**
     * 创建 weex 实例
     */
    private fun createWxInst() {
        mWeexInst = WXSDKInstance(mActivity)
        mWeexRender = WxRender(mActivity, mWeexInst, RenderListener())
        ManagerRegistry.getInst().onWxInstInit(mWxPage, mWeexInst, this)
    }


    fun close() {
        when (mHost) {
            is WxActivity       -> mHost.finish()
            is WxFragment       -> mHost.activity?.finish()
            is WxDialogFragment -> mHost.dismiss()
        }
    }

    inner class RenderListener : IWXRenderListener {
        override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            mRenderStatus = RenderStatus.RENDER_SUCCESS
            mLoadingHandler.finishWeexLoading(mContainerView)
            mWeexDebugger?.onRenderSuccess(instance, width, height)
        }

        override fun onViewCreated(instance: WXSDKInstance?, view: View?) {
            this@WxDelegate.onViewCreated(view)
        }

        override fun onException(instance: WXSDKInstance?, errCode: String?, msg: String?) {
            mWeexDebugger?.onException(instance, errCode, msg)
            CubeWx.mWxReportAdapter.report(IWxReportAdapter.CODE_RENDER_ERROR, """
                code = $errCode
                msg = $msg
                page = $mWxPage
            """.trimIndent())
            // 如果已经成功过，则此时不会走失败页面，只会没有反应
            if (mRenderStatus == RenderStatus.RENDER_SUCCESS) {
                return
            }
            if (mCurPage == null || mCurPage?.equals(mWxPage) == true) {
                renderNotFound()
            }
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
        }
    }

    //************************渲染页面*********************//

    internal fun refreshInstance() {
        val options = parseRenderOptions()
        options["refresh"] = "yes"
        mWeexInst.refreshInstance(options)
    }

    private val mRenderOpts by lazy {  mutableMapOf<String, Any>() }

    /**
     * 准备渲染的参数
     */
    private fun parseRenderOptions(): MutableMap<String, Any> {
        if(mRenderOpts.isNotEmpty()){
            return mRenderOpts
        }
        val uri = Uri.parse(mWxPage.h5Url)
        uri.queryParameterNames.forEach { mRenderOpts[it] = uri.getQueryParameter(it) ?: "" }
        mRenderOpts[INSTANCE_ID] = mWeexInst.instanceId
        val topSafeHeight = if (needImmersion) BarUI.getStatusbarHeight(mActivity) else 1
        mRenderOpts[TOP_SAFE_AREA_HEIGHT] = WxUtils.getWxPxByRealPx(topSafeHeight)
        mRenderOpts[BOTTOM_SAFE_AREA_HEIGHT] = 0
        mRenderOpts[BUNDLE_URL] = WxUtils.rewriteUrl(mWxPage.remoteJs, URIAdapter.BUNDLE)
        mRenderOpts[H5_URL] = WxUtils.rewriteUrl(mWxPage.h5Url, URIAdapter.WEB)
//        if (virtualHeight < 0) {
//            virtualHeight = Device.getVirtualBarHeight(mActivity)
//        }
        mRenderOpts[VIRTUAL_BAR_HEIGHT] = virtualHeight
        mWxPage.h5Url?.let {
            val data = ManagerRegistry.Data.getData(it)
            if (data != null) {
                mRenderOpts[EXTRA] = data
            }
        }
        return mRenderOpts
    }

    /**
     * 渲染之前处理
     */
    private fun preRender() {
        if (mRenderStatus.value >= RenderStatus.RENDER_BUNDLE_JS.value) {
            destroyWxInst()
            createWxInst()
            onCreate()
        }
        System.gc()
    }

    /**
     * 渲染页面
     */
    private fun render(page: WxPage) {
        preRender()
        mCurPage = page
        mRenderStatus = RenderStatus.RENDER_DOING
        mWeexRender.render(page, parseRenderOptions()) { mRenderStatus = RenderStatus.RENDER_BUNDLE_JS }
    }

    /**
     * 渲染当前页面
     */
    fun render() {
        render(mWxPage)
    }

    /**
     * 渲染指定的js，参数还是用本页面参数
     */
    fun renderJs(js: String) {
        preRender()
        mCurPage = mWxPage
        mWeexRender.renderJs(mWxPage, parseRenderOptions(), js)
    }

    /**
     * 渲染 not found 页面
     */
    fun renderNotFound() {
        val errPage = CubeWx.mWxRouter.findPage(CubeWx.mWxPageAdapter.getNotFoundPageUrl()) ?: return
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
        mWeexDebugger?.onResume()
    }

    override fun onPause() {
        mWeexInst.onActivityPause()
        mLifeCallbacks.forEach { it.onPause() }
        mWeexDebugger?.onPause()
    }

    override fun onStop() {
        mWeexInst.onActivityStop()
        mLifeCallbacks.forEach { it.onStop() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mWeexInst.onActivityResult(requestCode, resultCode, data)
        mLifeCallbacks.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onPermissionResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onPermissionResult(requestCode, resultCode, data)
        mLifeCallbacks.forEach { it.onPermissionResult(requestCode, resultCode, data) }
        // mWeexInst.onRequestPermissionsResult(requestCode,resultCode,data)
    }

    // 是否拦截返回键
    internal var mInterceptBackPressed = false

    fun onBackPressed() {
        mWeexInst.onBackPressed()
    }
}


enum class RenderStatus(val value: Int) {
    RENDER_NONE(1),
    RENDER_DOING(2),
    RENDER_BUNDLE_JS(3),
    RENDER_SUCCESS(4),
    RENDER_FAILURE(5),

}