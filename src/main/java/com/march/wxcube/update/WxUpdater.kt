package com.march.wxcube.update

import android.content.Context
import com.march.common.pool.ExecutorsPool
import com.march.common.utils.StreamUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.report
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.manager.RequestManager
import com.taobao.weex.common.WXResponse

/**
 * CreateAt : 2018/4/21
 * Describe : 配置更新管理
 * @author chendong
 */
class WxUpdater(private var url: String) {

    private val mDiskLruCache by lazy {
        DiskLruCache(WxUtils.makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }

    companion object {
        private const val CONFIG_KEY = "weex-config"
        private const val CACHE_DIR = "config-cache"
        private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    }

    /**
     * 1. 启动时尝试从 Local(config.json) 读取数据，来源是 Net，保证配置文件最新
     * 2. 无法读取到则尝试从 Assets(config.json) 读取，保证读取速度，完毕后存储到 Local 中
     * 3. 启动配置文件下载，下载完毕后存储到 Local 供下次启动时读取
     * 4. 当配置文件读取完毕，检索首页跳转
     */
    fun update(context: Context) {
        ExecutorsPool.getInst().execute {
            // 磁盘缓存读取
            var configJson = mDiskLruCache.read(CONFIG_KEY)
            if (configJson.isBlank()) { // assets 读取
                configJson = readAssets(context, "config/config.json")
            }
            parseJsonAndUpdate(context, configJson)
            // 发起网络，并存文件
            val request = ManagerRegistry.Request.makeWxRequest(url = url, from = "request-wx-config")
            ManagerRegistry.Request.request(request, object : HttpListener {
                override fun onHttpFinish(response: WXResponse) {
                    if (response.errorCode == RequestManager.ERROR_CODE_FAILURE) {
                        report("请求配置文件失败")
                    } else {
                        val netJson = response.data ?: return
                        mDiskLruCache.write(CONFIG_KEY, netJson)
                        parseJsonAndUpdate(context, netJson)
                    }
                }
            }, false)
        }
    }


    // 解析配置文件，并通知出去
    private fun parseJsonAndUpdate(context: Context, json: String) {
        if (json.isBlank())
            return
        try {
            val weexPagesResp = CubeWx.mWxModelAdapter.convert(json)
            val weexPages = weexPagesResp?.datas
            weexPages?.let {
                val pages = PageFilter.filter(it)
                pages.forEach {
                    it.h5Url = ManagerRegistry.Host.makeWebUrl(it.h5Url?:"")
                    if (weexPagesResp.indexPage == it.pageName) {
                        it.indexPage = true
                    }
                }
                CubeWx.onWeexConfigUpdate(context, pages)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
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
