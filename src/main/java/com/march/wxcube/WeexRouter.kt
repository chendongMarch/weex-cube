package com.march.wxcube

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.march.wxcube.common.report
import com.march.wxcube.model.DialogConfig

import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexActivity
import com.march.wxcube.ui.WeexDialogFragment

/**
 * CreateAt : 2018/3/27
 * Describe : weex 路由管理
 *
 * @author chendong
 */
class WeexRouter {

    // url-page 的 map，url 需要是不带有协议头的、没有参数的 url
    private var mWeexPageMap = mutableMapOf<UrlKey, WeexPage>()

    private class UrlKey {
        internal var host = ""
        internal var port = ""
        internal var path = ""

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
    fun openUrl(context: Context, url: String) {
        val page = findPage(url) ?: return
        val intent = Intent(context, WeexActivity::class.java)
        intent.putExtra(WeexPage.KEY_PAGE, page)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Weex.getInst().mWeexInjector.onErrorReport(e, "open Url, can not start activity, url => $url")
        }

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
        val weexPage = mWeexPageMap[UrlKey.fromUrl(url)]
        if (weexPage == null) {
            report("open Url, can not find page, url => $url")
            val page = WeexPage()
            page.webUrl = url
            return page
        }
        return weexPage.make(url)
    }

    fun update(weexPages: List<WeexPage>) {
        mWeexPageMap.isNotEmpty().let { mWeexPageMap.clear() }
        weexPages.forEach {
            mWeexPageMap[WeexRouter.UrlKey.fromUrl(it.webUrl!!)] = it
        }
    }

}
