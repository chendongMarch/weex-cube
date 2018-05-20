package com.march.wxcube

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import com.march.wxcube.common.report
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig

import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDialogFragment

/**
 * CreateAt : 2018/3/27
 * Describe : weex 路由管理
 *
 * @author chendong
 */
class WeexRouter : WeexUpdater.UpdateHandler {

    // url-page 的 map，url 需要是不带有协议头的、没有参数的 url
    private var mWeexPageMap = mutableMapOf<UrlKey, WeexPage>()

    private class UrlKey {
        internal var host: String? = ""
        internal var port: String? = ""
        internal var path: String? = ""

        override fun equals(other: Any?): Boolean {
            if (other == null || other !is UrlKey) {
                return false
            }
            return host == other.host && port == other.port && path == other.path
        }

        override fun hashCode(): Int {
            return 43
        }

        companion object {
            internal fun fromUrl(url: String): UrlKey {
                val urlKey = UrlKey()
                val uri = Uri.parse(url)
                urlKey.host = uri.host
                urlKey.port = uri.port.toString()
                urlKey.path = uri.path
                return urlKey
            }
        }
    }

    /**
     * 打开一个 web url
     */
    fun openUrl(context: Context, url: String): Boolean {
        val page = findPage(url) ?: return false
        val intent = Intent()
        intent.putExtra(WeexPage.KEY_PAGE, page)
        intent.data = Uri.parse("app://weex.cube/weex")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            report("open Url, can not start activity, url => $url", e)
            return false
        }
        return true
    }


    /**
     * 打开一个弹窗
     */
    fun openDialog(activity: AppCompatActivity, url: String, config: DialogConfig?) {
        val nonNullConfig = config ?: DialogConfig()
        val page = Weex.getInst().mWeexRouter.findPage(url) ?: return
        val fragment = WeexDialogFragment.newInstance(page, nonNullConfig)
        fragment.show(activity.supportFragmentManager, "dialog")
    }

    /**
     * 根据 web url 查找指定页面
     */
    fun findPage(url: String): WeexPage? {
        var safeUrl = ManagerRegistry.ENV.delHttp(url)
        safeUrl = ManagerRegistry.ENV.validUrl(safeUrl)
        val weexPage = mWeexPageMap[UrlKey.fromUrl(safeUrl)]
        if (weexPage == null) {
            report("open Url, can not find page, url => $url")
            val page = WeexPage()
            page.webUrl = url
            return page
        }
        return weexPage.make(url)
    }

    override fun onUpdateConfig(context: Context, weexPages: List<WeexPage>?) {
        mWeexPageMap.isNotEmpty().let { mWeexPageMap.clear() }
        weexPages?.forEach {
            mWeexPageMap[WeexRouter.UrlKey.fromUrl(it.webUrl!!)] = it
        }
        mRouterReadyCallback?.invoke()
    }

    var mRouterReadyCallback: (() -> Unit)? = null

    fun openIndexPage(context: Context): Boolean {
        if (mWeexPageMap.isEmpty()) {
            return false
        }
        var page: WeexPage? = null
        for (mutableEntry in mWeexPageMap) {
            if (mutableEntry.value.indexPage) {
                page = mutableEntry.value
                break
            }
        }
        if (page != null && !page.webUrl.isNullOrBlank()) {
            return Weex.getInst().mWeexRouter.openUrl(context, page.webUrl!!)
        }
        return false
    }

}
