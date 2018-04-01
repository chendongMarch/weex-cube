package com.march.wxcube

import android.app.Application

import com.march.wxcube.adapter.ImgAdapter
import com.march.wxcube.cache.JsBundleCache
import com.march.wxcube.model.PageBundle
import com.march.wxcube.module.BasicModule
import com.march.wxcube.module.DebugModule
import com.taobao.weex.InitConfig
import com.taobao.weex.WXEnvironment
import com.taobao.weex.WXSDKEngine
import com.taobao.weex.common.WXException

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class Weex private constructor() {

    var weexService: WeexService = WeexService.EMPTY
    val jsBundleCache: JsBundleCache = JsBundleCache() // 负责模版缓存
    val weexRouter: WeexRouter = WeexRouter() // 负责跳转逻辑
    var jsLoadStrategy = JsLoadStrategy.PREPARE_ALL

    object JsLoadStrategy {
        val ALWAYS_FRESH = 0 // 总是使用最新的
        val PREPARE_ALL = 1 // 提前准备
        val LAZY_LOAD = 2 // 使用时才加载，并缓存
    }

    fun init(application: Application, debug: Boolean, service: WeexService) {

        weexService = service

        WXEnvironment.setOpenDebugLog(debug)
        WXEnvironment.setApkDebugable(debug)
        WXSDKEngine.addCustomOptions("container", "weex-cube")

        val builder = InitConfig.Builder()
                // 用户行为捕捉
                // .setUtAdapter(new UtAdapter())
                // 网络请求 def
                // .setHttpAdapter(new OkHttpAdapter())
                // 存储管理
                // .setStorageAdapter(new StorageAdapter())
                // URI 重写 def
                // .setURIAdapter(new UriAdapter())
                // js 错误
                // .setJSExceptionAdapter(new JsErrorAdapter())
                // drawable 加载
                // .setDrawableLoader(new DrawableLoader())
                // web socket
                // .setWebSocketAdapterFactory(WebSocketAdapter.createFactory())
                // 图片加载
                .setImgAdapter(ImgAdapter())
        service.onInitWeex(builder)
        WXSDKEngine.initialize(application, builder.build())

        registerModule()

    }

    private fun registerModule() {
        try {
            WXSDKEngine.registerModule("basic", BasicModule::class.java, true)
            WXSDKEngine.registerModule("debug", DebugModule::class.java, true)
        } catch (e: WXException) {
            e.printStackTrace()
        }
    }

    companion object {
        val instance: Weex by lazy { Weex() }

        fun getInst() = instance
    }
}


