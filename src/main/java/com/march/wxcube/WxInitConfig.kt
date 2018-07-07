package com.march.wxcube

import android.content.Context
import com.march.wxcube.adapter.*
import com.march.wxcube.common.WxUtils
import com.march.wxcube.loader.JsCacheStrategy
import com.march.wxcube.loader.JsLoadStrategy
import com.march.wxcube.loader.JsPrepareStrategy
import com.march.wxcube.loader.WxJsLoader
import com.march.wxcube.router.WxRouter
import com.march.wxcube.update.WxUpdater


/**
 * CreateAt : 2018/4/20
 * Describe : 配置
 *
 * @author chendong
 */
class WxInitConfig {

    companion object {
        fun build(complete: WxInitConfig.() -> Unit): WxInitConfig {
            return WxInitConfig().apply(complete)
        }
    }

    var smallImgHolder: Int = 0
    var largeImgHolder: Int = 0

    var https: Boolean = false
    var debug: Boolean = true

    var jsLoadStrategy: Int = JsLoadStrategy.DEFAULT
    var jsCacheStrategy: Int = JsCacheStrategy.CACHE_MEMORY_DISK_BOTH
    var jsPrepareStrategy: Int = JsPrepareStrategy.PREPARE_ALL

    var configUrl: String = ""
    var reqAuthority: String = ""
    var bundleAuthority: String = ""
    var bundlePathPrefix: String = ""
    var webAuthority: String = ""

    var wxModelAdapter: IWxModelAdapter = DefaultWxModelAdapter()
    var wxDebugAdapter: IWxDebugAdapter = DefaultWxDebugAdapter()
    var wxInitAdapter: IWxInitAdapter = DefaultWxInitAdapter()
    var wxPageAdapter: IWxPageAdapter = DefaultWxPageAdapter()
    var wxReportAdapter: IWxReportAdapter = DefaultWxReportAdapter()


    fun prepare(ctx: Context): WxInitConfig {
        CubeWx.mWxCfg = this

        // adapter
        CubeWx.mWxModelAdapter = wxModelAdapter
        CubeWx.mWxDebugAdapter = wxDebugAdapter
        CubeWx.mWxInitAdapter = wxInitAdapter
        CubeWx.mWxPageAdapter = wxPageAdapter
        CubeWx.mWxReportAdapter = wxReportAdapter
        //
        CubeWx.mRootCacheDir = WxUtils.makeRootCacheDir()
        //
        CubeWx.mWxJsLoader = WxJsLoader(ctx, jsLoadStrategy, jsCacheStrategy, jsPrepareStrategy)
        CubeWx.mWxUpdater = WxUpdater(configUrl)
        CubeWx.mWxRouter = WxRouter()
        return this
    }
}