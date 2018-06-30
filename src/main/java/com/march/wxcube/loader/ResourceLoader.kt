package com.march.wxcube.loader

import android.content.Context
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WxPage
import com.taobao.weex.utils.WXFileUtils

/**
 * CreateAt : 2018/6/29
 * Describe :
 *
 * @author chendong
 */
interface ResourceLoader {
    fun load(context: Context, wxPage: WxPage): String?
}

//  网络加载器
class NetResourceLoader : ResourceLoader {

    override fun load(context: Context, wxPage: WxPage): String? {
        return downloadJs(wxPage)
    }

    private fun downloadJs(page: WxPage): String? {
        val url = page.remoteJs ?: return null
        val http = ManagerRegistry.Request
        val makeJsResUrl = ManagerRegistry.Host.makeJsResUrl(url)
        val wxRequest = http.makeWxRequest(url = makeJsResUrl, from = "download-js")
        val resp = http.requestSync(wxRequest, false)
        return resp.data
    }
}

// 文件加载器
class FileResourceLoader(private val jsFileCache: JsFileCache) : ResourceLoader {
    override fun load(context: Context, wxPage: WxPage): String? {
        return wxPage.localJs.let { jsFileCache.read(it) }
    }
}

// assets 加载器
class AssetsResourceLoader : ResourceLoader {
    override fun load(context: Context, wxPage: WxPage): String? {
        if (isAssetsExist(wxPage.assetsJs, context)) {
            return WXFileUtils.loadAsset("js/${wxPage.assetsJs}", context)
        }
        return null
    }

    private fun isAssetsExist(name: String?, context: Context): Boolean {
        if (name == null) {
            return false
        }
        return try {
            val files = context.assets.list("js")
            files.any { name == it }
        } catch (e: Exception) {
            false
        }
    }
}

// 内存加载器
class CacheResourceLoader(private val memoryCache: JsMemoryCache) : ResourceLoader {
    override fun load(context: Context, wxPage: WxPage): String? {
        return memoryCache.get(wxPage.key)
    }

}