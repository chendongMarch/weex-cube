package com.march.wxcube

import android.app.Application
import com.march.common.Common
import com.march.webkit.WebKit

import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.common.JsonParseAdapterImpl
import com.march.wxcube.module.*
import com.march.wxcube.widget.Container
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
    val weexBundleCache: WeexBundleCache = WeexBundleCache() // 负责模版缓存
    val weexRouter: WeexRouter = WeexRouter() // 负责跳转逻辑
    var jsLoadStrategy = JsLoadStrategy.PREPARE_ALL

    object JsLoadStrategy {
        const val ALWAYS_FRESH = 0 // 总是使用最新的
        const val PREPARE_ALL = 1 // 提前准备
        const val LAZY_LOAD = 2 // 使用时才加载，并缓存
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
        registerComponent()

        Common.init(application, JsonParseAdapterImpl())
        WebKit.init(application)
    }

    private fun registerComponent() {
        try {
            WXSDKEngine.registerComponent("container", Container::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun registerModule() {
        try {
            WXSDKEngine.registerModule("cube-basic", BasicModule::class.java, true)
            WXSDKEngine.registerModule("cube-debug", DebugModule::class.java, true)
            WXSDKEngine.registerModule("cube-statusbar", StatusBarModule::class.java, true)
            WXSDKEngine.registerModule("cube-modal", ModalModule::class.java, true)
            WXSDKEngine.registerModule("cube-event", EventModule::class.java, true)
        } catch (e: WXException) {
            e.printStackTrace()
        }
    }

    companion object {
        val instance: Weex by lazy { Weex() }

        fun getInst() = instance
    }
}


