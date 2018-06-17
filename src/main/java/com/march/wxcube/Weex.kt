package com.march.wxcube

import android.content.Context
import com.alibaba.android.bindingx.plugin.weex.BindingX
import com.march.common.Common
import com.march.common.CommonInjector
import com.march.common.adapter.JsonParser
import com.march.common.model.WeakContext
import com.march.common.utils.FileUtils
import com.march.webkit.WebKit
import com.march.wxcube.common.JsonParserImpl
import com.march.wxcube.common.sdFile
import com.march.wxcube.debug.WxDebugActivityLifeCycle
import com.march.wxcube.manager.*
import com.march.wxcube.model.WeexPage
import com.march.wxcube.module.OneModule
import com.march.wxcube.router.WeexRouter
import com.march.wxcube.update.WeexUpdater
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
object Weex {

    const val CACHE_DIR = "weex-cache"
    const val PAGE_WEB = 1
    const val PAGE_WEEX = 2
    const val PAGE_INDEX = 3

    private val mWeakCtx by lazy { WeakContext(mWeexConfig.ctx) } // 上下文虚引用
    internal val mWeexJsLoader by lazy { WeexJsLoader(mWeexConfig.ctx, mWeexConfig.jsLoadStrategy, mWeexConfig.jsCacheStrategy, mWeexConfig.jsPrepareStrategy) } // 加载 js
    internal val mWeexRouter by lazy { WeexRouter() } // 路由页面管理
    internal val mWeexUpdater by lazy { WeexUpdater(mWeexConfig.configUrl) } // weex 页面更新

    var mWeexInjector: WeexInjector = WeexInjector.EMPTY // 外部注入支持
    lateinit var mWeexConfig: WeexConfig

    fun init(config: WeexConfig, injector: WeexInjector) {
        mWeexConfig = config.prepare()
        mWeexInjector = injector
        val ctx = config.ctx

        ctx.registerActivityLifecycleCallbacks(WxDebugActivityLifeCycle())
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
        if (Weex.mWeexConfig.debug) {
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
        if (Weex.mWeexConfig.debug) {
            rootFile = sdFile
        }
        val cacheFile = File(rootFile, CACHE_DIR)
        if(cacheFile.exists()) {
            FileUtils.delete(cacheFile)
        }
    }

    fun onWeexConfigUpdate(context: Context, pages: List<WeexPage>?) {
        mWeexRouter.onWeexCfgUpdate(context, pages)
        mWeexJsLoader.onWeexCfgUpdate(context, pages)
    }
}


