package com.march.wxcube.router

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.march.common.Common
import com.march.webkit.WebKit
import com.march.wxcube.Weex
import com.march.wxcube.common.report
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WebActivity
import com.march.wxcube.ui.WeexDialogFragment
import com.march.wxcube.update.OnWeexUpdateListener

/**
 * CreateAt : 2018/3/27
 * Describe : weex 路由管理
 * url 必须有一级路径，也就是必须有 /
 * @author chendong
 */
class WeexRouter : OnWeexUpdateListener {


    // url-page 的 map，url 需要是不带有协议头的、没有参数的 url
    internal var mWeexPageMap = mutableMapOf<UrlKey, WeexPage>()
    internal var mInterceptor: ((String) -> WeexPage?)? = null
    internal var mRouterReadyCallback: (() -> Unit)? = null

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
    fun openUrl(ctx: Context, url: String): Pair<Boolean, String> {
        val page = findPage(url) ?: return openWeb(ctx, url)
        val intent = Intent()
        intent.putExtra(WeexPage.KEY_PAGE, page)
        intent.data = Uri.parse("cube://${Common.BuildConfig.APPLICATION_ID}.weex/weex")
        return start(ctx, intent)
    }

    /**
     * 打开 weex 页面
     */
    fun openWeexPage(ctx: Context, page: WeexPage): Pair<Boolean, String> {
        val intent = Intent()
        intent.putExtra(WeexPage.KEY_PAGE, page)
        intent.data = Uri.parse("cube://${Common.BuildConfig.APPLICATION_ID}.weex/weex")
        return start(ctx, intent)
    }

    /**
     * 内置 webview 打开 web
     */
    fun openWeb(ctx: Context?, webUrl: String): Pair<Boolean, String> {
        val intent = Intent(ctx, WebActivity::class.java)
        intent.putExtra(WebKit.KEY_URL, ManagerRegistry.HOST.makeWebUrl(webUrl))
        return start(ctx, intent)
    }

    /**
     * 系统浏览器打开 web
     */
    fun openBrowser(ctx: Context?, webUrl: String): Pair<Boolean, String> {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(ManagerRegistry.HOST.makeWebUrl(webUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return start(ctx, intent)
    }

    /**
     * 打开一个弹窗
     */
    fun openDialog(activity: AppCompatActivity, url: String, config: DialogConfig?): Pair<Boolean, String> {
        val nonNullConfig = config ?: DialogConfig()
        val page = Weex.mWeexRouter.findPage(url)
                ?: return false to "WeexRouter#openDialog can not find page"
        val fragment = WeexDialogFragment.newInstance(page, nonNullConfig)
        fragment.show(activity.supportFragmentManager, "dialog")
        return true to "WeexRouter#openDialog success"
    }

    /**
     * 根据 web url 查找指定页面
     */
    fun findPage(url: String): WeexPage? {
        if (url.isBlank()) {
            return null
        }
        val result = if (url.indexOf("/") == -1) {
            // 通过 pageName 查找
            mInterceptor?.invoke(url) ?: mWeexPageMap.values.firstOrNull {
                it.pageName == url
            }
        } else {
            // 通过 url 查找
            val validUrl = ManagerRegistry.HOST.makeWebUrl(url)
            mInterceptor?.invoke(validUrl) ?: mWeexPageMap[UrlKey.fromUrl(validUrl)]

        }
        if (result == null) {
            report("open Url, can not find page, url => $url")
            return null
        }
        return result.make(url)
    }


    override fun onWeexCfgUpdate(context: Context, weexPages: List<WeexPage>?) {
        mWeexPageMap.isNotEmpty().let { mWeexPageMap.clear() }
        weexPages?.forEach {
            it.webUrl?.let { url ->
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
            return Weex.mWeexRouter.openUrl(context, page.webUrl ?: "").first
        }
        return false
    }

}
