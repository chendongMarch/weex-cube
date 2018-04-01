package com.march.wxcube

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils

import com.march.wxcube.model.PageBundle
import com.march.wxcube.ui.WeexActivity

import java.util.HashMap

/**
 * CreateAt : 2018/3/27
 * Describe : weex 路由管理
 *
 * @author chendong
 */
class WeexRouter {

    // url-page的map，url 需要是不带有协议头的、没有参数的 url
    private var mPageBundleMap = mutableMapOf<UrlKey, PageBundle>()

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
        intent.putExtra(PageBundle.KEY_PAGE, page)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Weex.instance.weexService.onErrorReport(e, "open Url, can not start activity, url => $url" )
        }

    }

    /**
     * 根据 web url 查找指定页面
     */
    fun findPage(url: String): PageBundle? {
        val pageBundle = mPageBundleMap[UrlKey.fromUrl(url)]
        if (pageBundle == null) {
            Weex.instance.weexService.onErrorReport(null, "open Url, can not find page, url => " + url)
            return null
        }
        pageBundle.realUrl = url
        return pageBundle
    }

    /**
     * 更新数据源
     */
    fun update(pageBundles: List<PageBundle>) {
        mPageBundleMap.isNotEmpty().let { mPageBundleMap.clear() }
        pageBundles
                .filterNot { TextUtils.isEmpty(it.webUrl) }
                .forEach { mPageBundleMap[UrlKey.fromUrl(it.webUrl!!)] = it }
    }
}
