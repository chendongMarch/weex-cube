package com.march.wxcube.common

import android.content.Context
import com.march.common.model.WeakContext
import com.march.common.pool.ExecutorsPool
import com.march.common.utils.StreamUtils
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.manager.RequestManager
import com.taobao.weex.common.WXResponse

/**
 * CreateAt : 2018/7/13
 * Describe : json 数据同步方案
 *
 * @author chendong
 */

typealias DataParser = (WeakContext, String) -> Boolean


class JsonSyncMgr(private val cfg: SyncCfg,
                  private val parser: DataParser) {

    data class SyncCfg(val key: String,
                       val url: String)

    private val mDiskLruCache by lazy {
        DiskLruCache(WxUtils.makeCacheDir(cfg.key), Long.MAX_VALUE)
    }

    /**
     * 1. 启动时尝试从 文件 读取数据，来源是 Net，保证配置文件最新
     * 2. 无法读取到则尝试从 Assets 读取，保证读取速度，完毕后存储到 文件 中
     * 3. 启动配置文件下载，下载完毕后存储到 Local 供下次启动时读取
     */
    fun update(context: Context) {
        val weakCtx = context.weak()
        ExecutorsPool.getInst().execute {
            var json = mDiskLruCache.read(cfg.key)
            if (json.isBlank()) {
                weakCtx.get()?.let {
                    json = readAssets(it, "${cfg.key}/${cfg.key}.json")
                }
            }
            parser(weakCtx, json)
            // 发起网络请求最新配置
            val request = ManagerRegistry.Request.makeWxRequest(url = cfg.url, from = cfg.key)
            ManagerRegistry.Request.request(request, false, object : HttpListener {
                override fun onHttpFinish(response: WXResponse) {
                    if (response.errorCode == RequestManager.ERROR_CODE_FAILURE) {
                        log("${cfg.key}  请求配置文件失败")
                    } else if (parser(weakCtx, response.data)) {
                        mDiskLruCache.write(cfg.key, response.data)
                    }
                    response.data = null
                }
            })
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