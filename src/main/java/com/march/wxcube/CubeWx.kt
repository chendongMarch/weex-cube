package com.march.wxcube

import android.app.Application
import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.march.common.model.WeakContext
import com.march.webkit.WebKit
import com.march.wxcube.adapter.*
import com.march.wxcube.common.WxInstaller
import com.march.wxcube.debug.WxDebugActivityLifeCycle
import com.march.wxcube.debug.WxGlobalDebugger
import com.march.wxcube.func.loader.WxJsLoader
import com.march.wxcube.func.router.WxRouter
import com.march.wxcube.func.update.WxUpdater
import com.march.wxcube.http.OkHttpMaker
import com.march.wxcube.manager.*
import com.march.wxcube.model.WxPage
import com.march.wxcube.wxadapter.ImgAdapter
import com.march.wxcube.wxadapter.JsErrorAdapter
import com.march.wxcube.wxadapter.OkHttpAdapter
import com.march.wxcube.wxadapter.UriAdapter
import com.taobao.weex.InitConfig
import com.taobao.weex.WXEnvironment
import com.taobao.weex.WXSDKEngine
import java.io.File
import java.io.InputStream

/**
 * CreateAt : 2018/3/26
 * Describe : Weex 管理类
 *
 * @author chendong
 */
object CubeWx {

    // model
    lateinit var mWxCfg: WxInitConfig
    lateinit var mWeakCtx: WeakContext
    // func
    lateinit var mWxJsLoader: WxJsLoader
    lateinit var mWxUpdater: WxUpdater
    lateinit var mWxRouter: WxRouter
    // adapter
    lateinit var mWxModelAdapter: IWxModelAdapter
    lateinit var mWxDebugAdapter: IWxDebugAdapter
    lateinit var mWxInitAdapter: IWxInitAdapter
    lateinit var mWxPageAdapter: IWxPageAdapter
    lateinit var mWxReportAdapter: IWxReportAdapter

    lateinit var mRootCacheDir:File

    fun init(ctx: Application, config: WxInitConfig) {
        ctx.registerActivityLifecycleCallbacks(WxDebugActivityLifeCycle())
        mWeakCtx = WeakContext(ctx)
        mWxCfg = config.prepare()
        WebKit.init(ctx, WebKit.CORE_SYS, null)
        WXEnvironment.setOpenDebugLog(config.debug)
        WXEnvironment.setApkDebugable(config.debug)

        val builder = InitConfig.Builder()
                // 用户行为捕捉
                // .setUtAdapter(new UtAdapter())
                // 网络请求 def
                .setHttpAdapter(OkHttpAdapter())
                // 存储管理，加缓存也并没有变快，读取数据库速度也还可以
                // .setStorageAdapter(StorageAdapter(mWeakCtx.get()))
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

        mWxInitAdapter.onWxSdkEngineInit(builder)
        WXSDKEngine.initialize(ctx, builder.build())
        WxInstaller.registerModule()
        WxInstaller.registerComponent()
        WxInstaller.registerBindingX()
        mWxInitAdapter.onWxModuleCompRegister()

        ManagerRegistry.getInst().register(MemoryDataMgr())
        ManagerRegistry.getInst().register(EventBusMgr())
        ManagerRegistry.getInst().register(RequestMgr())
        ManagerRegistry.getInst().register(WxInstMgr())
        ManagerRegistry.getInst().register(OnlineCfgMgr())
        ManagerRegistry.getInst().register(ResMappingMgr())

        mWxInitAdapter.onInitFinished()


        // 初始化 glide
        Glide.get(ctx).registry.replace(GlideUrl::class.java,
                InputStream::class.java,
                OkHttpUrlLoader.Factory(OkHttpMaker.buildGlideOkHttpClient()))
    }

    fun onWeexConfigUpdate(context: Context, pages: List<WxPage>?) {
        mWxRouter.onWeexCfgUpdate(context, pages)
        mWxJsLoader.onWeexCfgUpdate(context, pages)
        WxGlobalDebugger.init()
    }
}


