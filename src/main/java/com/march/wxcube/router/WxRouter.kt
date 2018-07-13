package com.march.wxcube.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.march.common.Common
import com.march.webkit.WebKit
import com.march.wxcube.CubeWx
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.log
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.model.WxPage
import com.march.wxcube.ui.WebActivity
import com.march.wxcube.ui.WxDialogFragment
import com.march.wxcube.update.OnWxUpdateListener
import com.taobao.weex.adapter.URIAdapter


/**
 * CreateAt : 2018/3/27
 * Describe : weex 路由管理
 * url 必须有一级路径，也就是必须有 /
 * @author chendong
 */
typealias Interceptor = (String) -> WxPage?

typealias Callback = () -> Unit

class WxRouter : OnWxUpdateListener {

    // url-page 的 map，url 需要是不带有协议头的、没有参数的 url
    internal var mWeexPageMap = mutableMapOf<UrlKey, WxPage>()
    internal var mInterceptor: Interceptor? = null
    internal var mRouterReadyCallback: Callback? = null

    /**
     * 开启页面
     */
    private fun start(ctx: Context?, intent: Intent?): Pair<Boolean, String> {
        if (ctx == null || intent == null) {
            return false to "WeexRouter#start ctx == null || intent == null"
        }
        return try {
            ctx.startActivity(intent)
            true to "WeexRouter#start success"
        } catch (e: Exception) {
            false to "WeexRouter#start error ${intent.dataString} ${e.message}"
        }
    }

    /**
     * 打开一个 web url
     */
    fun openUrl(ctx: Context, url: String, rewrite: (Intent) -> Intent = { i -> i }): Pair<Boolean, String> {
        val page = findPage(url) ?: return openWeb(ctx, url)
        val intent = Intent().apply {
            putExtra(WxPage.KEY_PAGE, page)
            data = Uri.parse("cube://${Common.BuildConfig.APPLICATION_ID}.weex/weex")
            rewrite(this)
        }
        return start(ctx, intent)
    }

    /**
     * 打开 weex 页面
     */
    fun openWeexPage(ctx: Context, page: WxPage): Pair<Boolean, String> {
        val intent = Intent()
        intent.putExtra(WxPage.KEY_PAGE, page)
        intent.data = Uri.parse("cube://${Common.BuildConfig.APPLICATION_ID}.weex/weex")
        return start(ctx, intent)
    }

    /**
     * 内置 webview 打开 web
     */
    fun openWeb(ctx: Context?, webUrl: String): Pair<Boolean, String> {
        val intent = Intent(ctx, WebActivity::class.java)
        intent.putExtra(WebKit.KEY_URL, WxUtils.rewriteUrl(webUrl, URIAdapter.WEB))
        return start(ctx, intent)
    }

    /**
     * 系统浏览器打开 web
     */
    fun openBrowser(ctx: Context?, webUrl: String): Pair<Boolean, String> {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(WxUtils.rewriteUrl(webUrl, URIAdapter.WEB))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(ctx, intent)
    }

    /**
     * 打开一个弹窗
     */
    fun openDialog(activity: AppCompatActivity, url: String, config: DialogConfig?): Pair<Boolean, String> {
        val nonNullConfig = config ?: DialogConfig()
        val page = CubeWx.mWxRouter.findPage(url)
                ?: return false to "WeexRouter#openDialog can not find page"
        val fragment = WxDialogFragment.newInstance(page, nonNullConfig)
        fragment.show(activity.supportFragmentManager, "dialog")
        return true to "WeexRouter#openDialog success"
    }

    /**
     * 根据 web url 查找指定页面
     */
    fun findPage(url: String): WxPage? {
        if (url.isBlank()) {
            return null
        }
        // 总是 interceptor 优先查找
        val result = if (!url.contains("/")) {
            // 如果不包含 /，不是 url 路径，通过 pageName 查找，
            mInterceptor?.invoke(url) ?: mWeexPageMap.values.firstOrNull { it.pageName == url }
        } else {
            // 通过 url 查找
            val validUrl = WxUtils.rewriteUrl(url, URIAdapter.WEB)
            mInterceptor?.invoke(validUrl) ?: mWeexPageMap[UrlKey.fromUrl(validUrl)]
        }
        if (result == null) {
            log("open Url, can not find page, url => $url")
            return null
        }
        return result.make(url)
    }


    override fun onWeexCfgUpdate(context: Context, weexPages: List<WxPage>?) {
        mWeexPageMap.isNotEmpty().let { mWeexPageMap.clear() }
        weexPages?.forEach {
            it.h5Url?.let { url ->
                mWeexPageMap[UrlKey.fromUrl(url)] = it
            }
        }
        mRouterReadyCallback?.invoke()
    }


    fun openIndexPage(context: Context): Boolean {
        if (mWeexPageMap.isEmpty()) {
            return false
        }
        val page = mWeexPageMap.values.firstOrNull { it.indexPage }
        if (page?.isValid == true) {
            return CubeWx.mWxRouter.openUrl(context, page.h5Url ?: "").first
        }
        return false
    }

}
