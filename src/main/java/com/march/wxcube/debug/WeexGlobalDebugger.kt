package com.march.wxcube.debug

import com.alibaba.fastjson.JSON
import com.march.wxcube.Weex
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
 * Describe : 全局调试管理
 *
 * 多页面调试支持：
 * 1. 拉取远程调试配置
 * 2. 老页面，根据线上配置完善
 * 3. 新页面，检测字段完整
 * 4. 生成调试配置 map
 *
 * 全局调试支持：
 * 更改整个 js host 即可
 *
 * @author chendong
 */
internal class WeexGlobalDebugger {

    private val mDiskLruCache by lazy {
        DiskLruCache(Weex.getInst().makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }

    private val mExecutorService by lazy { Executors.newCachedThreadPool() }

    companion object {
        private const val CONFIG_KEY = "weex-debug-config"
        private const val CACHE_DIR = "debug-config-cache"
        private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    }

    class WeexDebugPagesResp {
        var total: Int? = 0
        var datas: List<WeexPage>? = null
    }

    // 从磁盘初始化
    fun updateFromDisk() {
        // 磁盘缓存读取以前的配置
        val configJson = mDiskLruCache.read(CONFIG_KEY) ?: ""
        parseDebugJsonAndUpdate(configJson)
    }

    // 从网络初始化
    fun updateFromNet(url: String) {
        // 发起网络，并存文件
        val request = ManagerRegistry.REQ.makeWxRequest(url = url, from = "request-wx-debug-config")
        ManagerRegistry.REQ.request(request, object : HttpListener {
            override fun onHttpFinish(response: WXResponse) {
                if (response.errorCode == RequestManager.ERROR_CODE_FAILURE) {
                    report("请求调试配置文件失败")
                } else {
                    val netJson = response.data ?: return
                    mDiskLruCache.write(CONFIG_KEY, netJson)
                    parseDebugJsonAndUpdate(netJson)
                }
            }
        }, false)
    }


    // 解析配置文件，并通知出去
    private fun parseDebugJsonAndUpdate(json: String) {
        if (json.isBlank()) return
        try {
            val weexPagesResp = JSON.parseObject(json, WeexPagesResp::class.java)
            val weexPages = weexPagesResp?.datas
            val pages = weexPages ?: return
            val validPages = pages.filter { it.isValid }
            validPages.forEach {
                it.webUrl = ManagerRegistry.HOST.makeWebUrl(it.webUrl!!)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
