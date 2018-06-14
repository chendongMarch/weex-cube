package com.march.wxcube

import com.alibaba.android.bindingx.plugin.weex.BindingX
import com.facebook.stetho.Stetho
import com.march.common.Common
import com.march.common.CommonInjector
import com.march.common.adapter.JsonParser
import com.march.common.model.WeakContext
import com.march.common.utils.FileUtils
import com.march.webkit.WebKit
import com.march.wxcube.common.JsonParserImpl
import com.march.wxcube.common.sdFile
import com.march.wxcube.manager.*
import com.march.wxcube.module.OneModule
import com.march.wxcube.widget.Container
import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.wxadapter.JsErrorAdapter
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
    val mWeexJsLoader by lazy { WeexJsLoader(mWeexConfig.ctx, mWeexConfig.jsLoadStrategy, mWeexConfig.jsCacheStrategy, mWeexConfig.jsPrepareStrategy) } // 加载 js
    val mWeexRouter by lazy { WeexRouter() } // 路由页面管理
    val mWeexUpdater by lazy { WeexUpdater(mWeexConfig.configUrl) } // weex 页面更新

    var mWeexInjector: WeexInjector = WeexInjector.EMPTY // 外部注入支持
    lateinit var mWeexConfig: WeexConfig

    private fun init(config: WeexConfig, injector: WeexInjector) {
        mWeexConfig = config.prepare()
        mWeexInjector = injector
        val ctx = config.ctx

        Stetho.initialize(Stetho.newInitializerBuilder(ctx)
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(ctx))
                .build())
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
                .setJSExceptionAdapter(JsErrorAdapter())
                // drawable 加载
                // .setDrawableLoader(new DrawableLoader())
                // web socket
                // .setWebSocketAdapterFactory(WebSocketAdapter.createFactory())
                // 图片加载
                .setImgAdapter(ImgAdapter())
        injector.onWxSdkEngineInit(builder)
        WXSDKEngine.initialize(ctx, builder.build())
        registerModule()
        registerComponent()
        registerBindingX()
        injector.onWxModuleCompRegister()

        ManagerRegistry.getInst().register(DataManager.instance)
        ManagerRegistry.getInst().register(EventManager.instance)
        ManagerRegistry.getInst().register(RequestManager.instance)
        ManagerRegistry.getInst().register(HostManager.instance)
        ManagerRegistry.getInst().register(WeexInstManager.instance)

        ManagerRegistry.HOST.mWebHost = config.webHost
        ManagerRegistry.HOST.mJsResHost = config.jsResHost
        ManagerRegistry.HOST.mApiHost = config.apiHost

        Common.init(ctx, object : CommonInjector {
            override fun getConfigClass(): Class<*> {
                return mWeexInjector.getConfigClass()
            }

            override fun getJsonParser(): JsonParser {
                return JsonParserImpl()
            }
        })
        WebKit.init(ctx, WebKit.CORE_SYS, null)

        mWeexUpdater.registerUpdateHandler(mWeexRouter)
        mWeexUpdater.registerUpdateHandler(mWeexJsLoader)
    }

    private fun registerBindingX() {
        try {
            BindingX.register()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            WXSDKEngine.registerModule("bridge", OneModule::class.java, true)
        } catch (e: WXException) {
            e.printStackTrace()
        }
    }

    fun makeCacheDir(key: String): File {
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

    fun clearDiskCache(){
        val sdFile = sdFile()
        var rootFile = getContext()?.cacheDir ?: sdFile
        if (Weex.getInst().mWeexConfig.debug) {
            rootFile = sdFile
        }
        val cacheFile = File(rootFile, CACHE_DIR)
        if(cacheFile.exists()) {
            FileUtils.delete(cacheFile)
        }
    }

    companion object {
        const val CACHE_DIR = "weex-cache"
        const val PAGE_WEB = 1
        const val PAGE_WEEX = 2
        const val PAGE_INDEX = 3
        private val instance: Weex by lazy { Weex() }

        fun getInst() = instance

        fun init(config: WeexConfig, injector: WeexInjector) {
            getInst().init(config, injector)
        }
    }
}


