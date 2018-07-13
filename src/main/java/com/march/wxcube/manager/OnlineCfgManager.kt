package com.march.wxcube.manager

import android.content.Context
import com.march.common.pool.ExecutorsPool
import com.march.common.utils.StreamUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.log
import com.march.wxcube.func.update.PageFilter
import com.march.wxcube.http.HttpListener
import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.URIAdapter
import com.taobao.weex.common.WXResponse

/**
 * CreateAt : 2018/7/13
 * Describe :
 *
 * @author chendong
 */
class OnlineCfgManager : IManager {

    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {

    }

    private val mDiskLruCache by lazy {
        DiskLruCache(WxUtils.makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }

    companion object {
        private const val CONFIG_KEY = "online-config"
        private const val CACHE_DIR = "online-config"
        private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    }

    fun update(context: Context) {
        ExecutorsPool.getInst().execute {
            // 磁盘缓存读取
            var configJson = mDiskLruCache.read(CONFIG_KEY)
            // assets 读取
            if (configJson.isBlank()) {
                configJson = readAssets(context, "config/config.json")
            }
            // 解析缓存
            parseJsonAndUpdate(context, configJson)
            // 发起网络请求最新配置
            val request = ManagerRegistry.Request.makeWxRequest(url = CubeWx.mWxCfg.configUrl, from = "request-wx-config")
            ManagerRegistry.Request.request(request, false, object : HttpListener {
                override fun onHttpFinish(response: WXResponse) {
                    if (response.errorCode == RequestManager.ERROR_CODE_FAILURE) {
                        log("请求配置文件失败")
                    } else if (parseJsonAndUpdate(context, response.data)) {
                        mDiskLruCache.write(CONFIG_KEY, response.data)
                    }
                    response.data = null
                }
            })
        }
    }

    // 解析配置文件，并通知出去
    private fun parseJsonAndUpdate(context: Context, json: String?): Boolean {
        json ?: return false
        if (json.isBlank()) return false
        try {
            val wxPageResp = CubeWx.mWxModelAdapter.convert(json)
            val wxPages = wxPageResp?.datas ?: return false
            val filterPages = PageFilter.filter(context, wxPages)
            filterPages.forEach {
                it.h5Url = WxUtils.rewriteUrl(it.h5Url, URIAdapter.WEB)
                // it.remoteJs = WxUtils.rewriteUrl(it.remoteJs, URIAdapter.BUNDLE)
                if (wxPageResp.indexPage == it.pageName) {
                    it.indexPage = true
                }
            }
            CubeWx.onWeexConfigUpdate(context, filterPages)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun readAssets(context: Context, assetsPath: String): String {
        return try {
            StreamUtils.saveStreamToString(context.assets.open(assetsPath))
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}
