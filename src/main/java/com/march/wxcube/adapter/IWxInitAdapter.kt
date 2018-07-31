package com.march.wxcube.adapter

import com.march.wxcube.BuildConfig
import com.march.wxcube.module.dispatcher.BaseDispatcher
import com.taobao.weex.InitConfig
import okhttp3.OkHttpClient

/**
 * CreateAt : 2018/6/27
 * Describe : weex 初始化配置
 *
 * @author chendong
 */
interface IWxInitAdapter {

    /**
     * 网络请求初始化，自动移
     */
    fun onInitOkHttpClient(builder: OkHttpClient.Builder) {}

    /**
     * 初始化 glide
     */
    fun onInitGlideOkHttpClient(builder: OkHttpClient.Builder) {}

    /**
     * 初始化 WxSdkEngine
     */
    fun onWxSdkEngineInit(builder: InitConfig.Builder) {}

    /**
     * 注册 module,component
     */
    fun onWxModuleCompRegister() {}

    /**
     * 获取构建配置
     */
    fun getConfigClass(): Class<*> = BuildConfig::class.java

    /**
     * 获取要注入的 dispatcher
     */
    fun getModuleDispatchers(): Array<BaseDispatcher> = arrayOf()

    /**
     * 获取应用
     */
    fun getAppKey() = "cubewx"

    /**
     * 初始化结束
     */
    fun onInitFinished() {

    }

    /**
     * 通过 url 映射到 res
     */
    fun getUrlResMap(): Map<String, Int> {
        return mapOf()
    }

}

class DefaultWxInitAdapter : IWxInitAdapter