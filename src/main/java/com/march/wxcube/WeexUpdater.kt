package com.march.wxcube

import android.content.Context
import com.alibaba.fastjson.JSON
import com.march.common.utils.StreamUtils
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.report
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.manager.RequestManager
import com.march.wxcube.model.WeexPage
import com.taobao.weex.common.WXResponse
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/4/21
 * Describe :
 * @author chendong
 */
class WeexUpdater(private val url: String) {

    interface UpdateHandler {
        fun onUpdateConfig(context: Context, weexPages: List<WeexPage>?)
    }

    private val mDiskLruCache by lazy {
        DiskLruCache(Weex.getInst().makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }
    private val mExecutorService by lazy { Executors.newCachedThreadPool() }
    private val mUpdateHandlers by lazy { mutableListOf<UpdateHandler>() }

    companion object {
        private const val CONFIG_KEY = "weex-config"
        private const val CACHE_DIR = "config-cache"
        private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    }

    class WeexPagesResp {
        var total: Int? = 0
        var datas: List<WeexPage>? = null
    }

    /**
     * 1. 启动时尝试从 Local(config.json) 读取数据，来源是 Net，保证配置文件最新
     * 2. 无法读取到则尝试从 Assets(config.json) 读取，保证读取速度，完毕后存储到 Local 中
     * 3. 启动配置文件下载，下载完毕后存储到 Local 供下次启动时读取
     * 4. 当配置文件读取完毕，检索首页跳转
     */
    fun update(context: Context) {
        mExecutorService.execute {
            // 磁盘缓存读取
            var configJson = mDiskLruCache.read(CONFIG_KEY)
            if (configJson.isNullOrBlank()) {
                // assets 读取
                configJson = readAssets(context, "config/config.json")
            }
            // 更新配置
            val json = configJson ?: {
                report("本地和assets都无法读取 config")
                ""
            }()
            parseJsonAndUpdate(context, json)
            // 发起网络，并存文件
            val request = ManagerRegistry.REQ.makeWxRequest(url = url, from = "request-wx-config")
            ManagerRegistry.REQ.request(request, object : HttpListener {
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
        if (json.isBlank()) return
        try {
            val weexPagesResp = JSON.parseObject(json, WeexPagesResp::class.java)
            val weexPages = weexPagesResp?.datas
            val pages = weexPages ?: return
            val validPages = pages.filter { it.isValid }
            validPages.forEach { it.webUrl = ManagerRegistry.ENV.validUrl(it.webUrl) }
            mUpdateHandlers.forEach { it.onUpdateConfig(context, pages) }
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

    fun registerUpdateHandler(updateHandler: UpdateHandler) {
        mUpdateHandlers.add(updateHandler)
    }

    fun unRegisterUpdateHandler(updateHandler: UpdateHandler) {
        mUpdateHandlers.remove(updateHandler)
    }


}
