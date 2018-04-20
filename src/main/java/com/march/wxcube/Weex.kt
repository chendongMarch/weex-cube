package com.march.wxcube

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.march.common.Common
import com.march.common.model.WeakContext
import com.march.webkit.WebKit

import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.common.JsonParseAdapterImpl
import com.march.wxcube.manager.DataManager
import com.march.wxcube.manager.EventManager
import com.march.wxcube.manager.HttpManager
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WeexPage
import com.march.wxcube.module.*
import com.march.wxcube.widget.Container
import com.march.wxcube.wxadapter.OkHttpAdapter
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

    lateinit var mWeakCtx: WeakContext
    lateinit var mWeexInjector: WeexInjector
    lateinit var mWeexJsLoader: WeexJsLoader
    lateinit var mWeexRouter: WeexRouter


    private fun checkWeexConfig(application: Application, config: WeexConfig) {
        if (config.jsCacheMaxSize == -1) {
            val activityManager: ActivityManager = application.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            config.jsCacheMaxSize = (activityManager.memoryClass * 1024 * 1024 * 0.3f).toInt()
        }
    }

    fun init(application: Application, config: WeexConfig, injector: WeexInjector) {

        checkWeexConfig(application, config)

        mWeakCtx = WeakContext(application.applicationContext)
        mWeexInjector = injector
        mWeexJsLoader = WeexJsLoader(config.jsLoadStrategy, config.jsCacheMaxSize)
        mWeexRouter = WeexRouter()

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
                // .setURIAdapter(new UriAdapter())
                // js 错误
                // .setJSExceptionAdapter(new JsErrorAdapter())
                // drawable 加载
                // .setDrawableLoader(new DrawableLoader())
                // web socket
                // .setWebSocketAdapterFactory(WebSocketAdapter.createFactory())
                // 图片加载
                .setImgAdapter(ImgAdapter())
        injector.onInitWeex(builder)
        WXSDKEngine.initialize(application, builder.build())

        registerModule()
        registerComponent()

        ManagerRegistry.getInst().register(DataManager.instance)
        ManagerRegistry.getInst().register(EventManager.instance)
        ManagerRegistry.getInst().register(HttpManager.instance)

        Common.init(application, JsonParseAdapterImpl())
        WebKit.init(application)
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

    /**
     * 更新数据源
     */
    fun updateWeexPages(context: Context, weexPages: List<WeexPage>) {
        val list = weexPages.filterNot { TextUtils.isEmpty(it.webUrl) }
        mWeexRouter.update(list)
        mWeexJsLoader.update(context, list)

    }


    companion object {

        private val instance: Weex by lazy { Weex() }

        fun getInst() = instance
    }
}


