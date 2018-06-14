package com.march.wxcube

import android.app.Activity
import android.view.WindowManager
import com.march.common.utils.LgUtils
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.wxcube.loading.Loading
import com.march.wxcube.loading.SimpleLoading
import com.march.wxcube.module.dispatcher.BaseDispatcher
import com.taobao.weex.InitConfig
import okhttp3.OkHttpClient

/**
 * CreateAt : 2018/4/1
 * Describe :
 *
 * @author chendong
 */
interface WeexInjector {

    data class WxBuildConfig(val versionCode: Int, val versionName: String, val debug: Boolean)

    /**
     * 错误打印
     */
    fun onErrorReport(throwable: Throwable?, errorMsg: String) {
        LgUtils.e("wx-error", errorMsg)
        throwable?.let { LgUtils.e("wx-error", it) }
    }

    /**
     * log 打印
     */
    fun onLog(tag: String, msg: String) = LgUtils.e(tag, msg)

    /**
     * 初始化 WxSdkEngine
     */
    fun onWxSdkEngineInit(builder: InitConfig.Builder) {}

    /**
     * 注册 module,component
     */
    fun onWxModuleCompRegister() {}

    /**
     * 内置 Activity 创建时调用
     * 默认行为 weex 页面透明状态栏
     * web 页面全屏显示
     */
    fun onPageCreated(activity: Activity, type: Int) {
        ImmersionStatusBarUtils.setStatusBarLightMode(activity)
        if (type == Weex.PAGE_WEEX || type == Weex.PAGE_INDEX) {
            ImmersionStatusBarUtils.translucent(activity)
        } else if (type == Weex.PAGE_WEB) {
            // ImmersionStatusBarColorUtils.setStatusBarColor(mAct, Color.parseColor("#ffffff"))
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    /**
     * 网络请求初始化，自动移
     */
    fun onInitOkHttpClient(builder: OkHttpClient.Builder) {}

    /**
     * loading
     * 容器渲染时支持蒙版
     * 使用内置首页的自定义界面
     */
    fun getLoading(): Loading = SimpleLoading()

    /**
     * 获取构建配置
     */
    fun getConfigClass(): Class<*> = BuildConfig::class.java

    /**
     * 获取要注入的 dispatcher
     */
    fun getModuleDispatchers(): Array<BaseDispatcher> = arrayOf()

    companion object {
        val EMPTY = object : WeexInjector {

        }
    }
}