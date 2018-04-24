package com.march.wxcube

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Environment
import android.text.TextUtils
import com.march.common.Common
import com.march.common.model.WeakContext
import com.march.webkit.WebKit
import com.march.webkit.WebKitInjector

import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.common.JsonParseAdapterImpl
import com.march.wxcube.manager.*
import com.march.wxcube.model.WeexPage
import com.march.wxcube.module.*
import com.march.wxcube.widget.Container
import com.march.wxcube.wxadapter.OkHttpAdapter
import com.march.wxcube.wxadapter.UriAdapter
import com.taobao.weex.InitConfig
import com.taobao.weex.WXEnvironment
import com.taobao.weex.WXSDKEngine
import com.taobao.weex.common.WXException
import java.net.HttpCookie

/**
 * CreateAt : 2018/3/26
 * Describe : Weex 管理类
 *
 * @author chendong
 */
class Weex private constructor() {

    lateinit var mWeakCtx: WeakContext // 上下文虚引用
    lateinit var mWeexInjector: WeexInjector // 外部注入支持
    lateinit var mWeexJsLoader: WeexJsLoader // 加载 js
    lateinit var mWeexRouter: WeexRouter // 路由页面管理
    lateinit var mWeexUpdater: WeexUpdater // weex 页面更新


    fun init(config: WeexConfig, injector: WeexInjector) {
        val context = config.application

        mWeexInjector = injector
        mWeakCtx = WeakContext(context)
        mWeexJsLoader = WeexJsLoader(config)
        mWeexRouter = WeexRouter()
        mWeexUpdater = WeexUpdater()

        WXEnvironment.setOpenDebugLog(config.debug)
        WXEnvironment.setApkDebugable(config.debug)
        WXSDKEngine.addCustomOptions("container", "weex-cube")

        val builder = InitConfig.Builder()
                // 用户行为捕捉
                // .setUtAdapter(new UtAdapter())
                // 网络请求 def
                .setHttpAdapter(OkHttpAdapter())
                // 存储管理
                // .setStorageAdapter(new StorageAdapter())
                // URI 重写 def
                .setURIAdapter(UriAdapter())
                // js 错误
                // .setJSExceptionAdapter(new JsErrorAdapter())
                // drawable 加载
                // .setDrawableLoader(new DrawableLoader())
                // web socket
                // .setWebSocketAdapterFactory(WebSocketAdapter.createFactory())
                // 图片加载
                .setImgAdapter(ImgAdapter())
        injector.onInitWeex(builder)
        WXSDKEngine.initialize(context, builder.build())

        registerModule()
        registerComponent()

        ManagerRegistry.getInst().register(DataManager.instance)
        ManagerRegistry.getInst().register(EventManager.instance)
        ManagerRegistry.getInst().register(HttpManager.instance)
        ManagerRegistry.getInst().register(EnvManager.instance)

        Common.init(context, JsonParseAdapterImpl())
        WebKit.init(context, WebKit.CORE_SYS,null)
    }

    fun getContext() = mWeakCtx.get()


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

        private val instance: Weex by lazy { Weex() }

        fun getInst() = instance
    }
}


