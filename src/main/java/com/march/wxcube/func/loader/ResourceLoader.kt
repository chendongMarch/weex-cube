package com.march.wxcube.func.loader

import android.content.Context
import com.march.wxcube.CubeWx
import com.march.wxcube.common.WxUtils
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.WxPage
import com.taobao.weex.adapter.URIAdapter
import com.taobao.weex.utils.WXFileUtils

/**
 * CreateAt : 2018/6/29
 * Describe :
 *
 * @author chendong
 */
interface ResourceLoader {
    fun load(context: Context?, wxPage: WxPage): String?
}

//  网络加载器
class NetResourceLoader : ResourceLoader {

    override fun load(context: Context?, wxPage: WxPage): String? {
        return downloadJs(wxPage)
    }

    private fun downloadJs(page: WxPage): String? {
        val url = page.remoteJs ?: return null
        val http = ManagerRegistry.Request
        val makeJsResUrl = WxUtils.rewriteUrl(url, URIAdapter.BUNDLE)
        val wxRequest = http.makeWxRequest(url = makeJsResUrl, from = "download-js")
        val resp = http.requestSync(wxRequest, false)
        return resp.data
    }
}

// 文件加载器
class FileResourceLoader(private val jsFileCache: JsFileCache) : ResourceLoader {
    override fun load(context: Context?, wxPage: WxPage): String? {
        return wxPage.localJs.let { jsFileCache.read(it) }
    }
}

// assets 加载器
class AssetsResourceLoader : ResourceLoader {
    override fun load(context: Context?, wxPage: WxPage): String? {
        context?.let {
            if (CubeWx.mWxJsLoader.isAssetsJsExist(it, wxPage)) {
                return WXFileUtils.loadAsset("js/${wxPage.assetsJs}", it)
            }
        }
        return null
    }
}

// 内存加载器
class CacheResourceLoader(private val memoryCache: JsMemoryCache) : ResourceLoader {
    override fun load(context: Context?, wxPage: WxPage): String? {
        return memoryCache.get(wxPage.key)
    }

}