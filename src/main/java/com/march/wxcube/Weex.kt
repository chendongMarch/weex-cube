package com.march.wxcube

import com.march.common.Common
import com.march.common.model.WeakContext
import com.march.webkit.WebKit
import com.march.wxcube.common.JsonParseAdapterImpl
import com.march.wxcube.common.sdFile
import com.march.wxcube.manager.*
import com.march.wxcube.module.*
import com.march.wxcube.widget.Container
import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.wxadapter.OkHttpAdapter
import com.march.wxcube.wxadapter.UriAdapter
import com.taobao.weex.InitConfig
import com.taobao.weex.WXEnvironment
import com.taobao.weex.WXSDKEngine
import com.taobao.weex.common.WXException
import java.io.File

/**
 * CreateAt : 2018/3/26
 * Describe : Weex 管理类
 *
 * @author chendong
 */
class Weex private constructor() {

    private val mWeakCtx by lazy { WeakContext(mWeexConfig.ctx) } // 上下文虚引用
    val mWeexJsLoader by lazy { WeexJsLoader(mWeexConfig.ctx, mWeexConfig.jsLoadStrategy, mWeexConfig.jsCacheStrategy) } // 加载 js
    val mWeexRouter by lazy { WeexRouter() } // 路由页面管理
    val mWeexUpdater by lazy { WeexUpdater(mWeexConfig.configUrl) } // weex 页面更新

    lateinit var mWeexConfig: WeexConfig
    lateinit var mWeexInjector: WeexInjector // 外部注入支持

    fun init(config: WeexConfig, injector: WeexInjector) {
        mWeexConfig = config.prepare()
        mWeexInjector = injector

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
        WXSDKEngine.initialize(config.ctx, builder.build())

        registerModule()
        registerComponent()

        ManagerRegistry.getInst().register(DataManager.instance)
        ManagerRegistry.getInst().register(EventManager.instance)
        ManagerRegistry.getInst().register(HttpManager.instance)
        ManagerRegistry.getInst().register(EnvManager.instance)
        ManagerRegistry.getInst().register(WeexInstManager.instance)

        ManagerRegistry.ENV.registerEnv(config.envs)
        ManagerRegistry.ENV.mNowEnv = config.nowEnv

        Common.init(config.ctx, JsonParseAdapterImpl())
        WebKit.init(config.ctx, WebKit.CORE_SYS, null)

        Weex.getInst().mWeexUpdater.requestPages(config.ctx)
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
        } catch (e: WXException) {
            e.printStackTrace()
        }
    }

    public fun makeCacheDir(key: String): File {
        val sdFile = sdFile()
        var rootFile = getContext()?.cacheDir ?: sdFile
        if (Weex.getInst().mWeexConfig.debug) {
            rootFile = sdFile
        }
        val cacheFile = File(rootFile, CACHE_DIR)
        cacheFile.mkdirs()
        val destDir = File(cacheFile, key)
        destDir.mkdirs()
        return destDir
    }

    companion object {
        const val CACHE_DIR = "weex-cache"
        const val PAGE_WEB = 1
        const val PAGE_WEEX = 2
        const val PAGE_INDEX = 3
        private val instance: Weex by lazy { Weex() }

        fun getInst() = instance
    }
}


