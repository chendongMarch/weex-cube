package com.march.wxcube

import android.app.Activity
import com.march.common.utils.LogUtils
import com.march.wxcube.loading.Loading
import com.march.wxcube.loading.SimpleLoading
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
        LogUtils.e("wx-error", errorMsg)
        throwable?.let { LogUtils.e("wx-error", it) }
    }

    /**
     * log 打印
     */
    fun onLog(tag: String, msg: String) {
        LogUtils.e(tag, msg)
    }

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
     */
    fun onPageCreated(activity: Activity, type: Int) {

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
    fun getLoading(): Loading {
        return SimpleLoading()
    }

    fun getBuildConfig(): WxBuildConfig

    companion object {
        val EMPTY = object : WeexInjector {
            override fun getBuildConfig(): WxBuildConfig {
                return WxBuildConfig(0, "0.0.0", false)
            }
        }
    }

}