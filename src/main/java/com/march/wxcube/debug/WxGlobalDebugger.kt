package com.march.wxcube.debug

import android.content.Context
import com.alibaba.fastjson.JSON
import com.march.common.pool.DiskKVManager
import com.march.common.utils.ToastUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.report
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.manager.RequestManager
import com.march.wxcube.model.WxPage
import com.march.wxcube.router.UrlKey
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
internal object WxGlobalDebugger {

    private const val CONFIG_KEY = "weex-debug-config"
    private const val CACHE_DIR = "debug-config-cache"
    private const val DISK_MAX_SIZE = Int.MAX_VALUE.toLong()
    private const val MAX_VERSION = "100.100.100"
    private const val MIN_VERSION = "0.0.0"
    private const val DEBUG_ENABLE = "DEBUG_ENABLE"
    private const val DEBUG_HOST = "DEBUG_HOST"

    data class DebugWxPagesResp(
            var global: Boolean = false,
            var autoJumpPage: String = "",
            var datas: List<WxPage> = listOf())

    private val mDiskLruCache by lazy {
        DiskLruCache(CubeWx.makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }

    private val mExecutorService by lazy { Executors.newCachedThreadPool() }
    private var mDebugWeexPagesResp: DebugWxPagesResp? = null
    internal var mWeexPageMap = mutableMapOf<UrlKey, WxPage>()


    private var mDebugHost = ""
    private var mDebugEnable = false

    internal fun init() {
        if (!checkDebug()) {
            return
        }
        mExecutorService.execute {
            updateFromDisk()
            updateFromNet()
        }
    }

    // 设置调试状态
    fun setDebugHost(host: String = "") {
        DiskKVManager.getInst().put(DEBUG_HOST, host)
    }

    // 设置调试状态
    fun setDebugEnable(debug: Boolean = false) {
        DiskKVManager.getInst().put(DEBUG_ENABLE, debug)
    }

    // 设置调试状态
    fun getDebugHost(): String {
        return DiskKVManager.getInst().get(DEBUG_HOST, "")
    }

    // 设置调试状态
    fun getDebugEnable(): Boolean {
        return DiskKVManager.getInst().get(DEBUG_ENABLE, false)
    }

    // 从磁盘初始化
    private fun updateFromDisk() {
        // 磁盘缓存读取以前的配置
        val configJson = mDiskLruCache.read(CONFIG_KEY)
        parseDebugJsonAndUpdate(configJson)
    }

    // 从网络初始化
    internal fun updateFromNet() {
        if (!checkDebug()) {
            return
        }
        val url = CubeWx.mWxDebugAdapter.makeDebugConfigUrl(mDebugHost)
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
        if (!checkDebug() || json.isBlank()) {
            return
        }
        try {
            val weexPagesResp = JSON.parseObject(json, DebugWxPagesResp::class.java)
            mDebugWeexPagesResp = weexPagesResp
            val originPages = weexPagesResp?.datas ?: return report("调试文件 datas = null")
            // 不管新老页面 pageName 是唯一标识
            val pages = mutableListOf<WxPage>()
            originPages.forEach {
                if (!it.pageName.isNullOrBlank()) {
                    val p = prepareOldPage(it) ?: prepareNewPage(it)
                    p?.let {
                        it.h5Url = ManagerRegistry.HOST.makeWebUrl(it.h5Url ?: "")
                        pages.add(it)
                    }
                }
            }
            updateWeexPageMap(pages)
        } catch (e: Exception) {
            e.printStackTrace()
            report(e.message ?: "", e)
            ToastUtils.show("调试配置文件解析失败 ${e.message}")
        }
    }

    // 如果是老页面将会根据线上配置完善相关数据
    private fun prepareOldPage(p: WxPage): WxPage? {
        var page = p
        val validOldPage = CubeWx.mWxRouter.mWeexPageMap.values.firstOrNull {
            it.pageName == page.pageName
        }
        return if (validOldPage != null) {
            page.pageName = validOldPage.pageName
            page.comment = validOldPage.comment
            page.jsVersion = MAX_VERSION
            page.appVersion = MIN_VERSION
            page.h5Url = page.h5Url ?: validOldPage.h5Url
            // page.remoteJs = page.remoteJs ?: debugWeexPageMaker(page, mHost)
            page.md5 = ""
            page = CubeWx.mWxDebugAdapter.completeDebugWeexPage(page, mDebugHost)
            if (page.h5Url.isNullOrBlank() || page.remoteJs.isNullOrBlank()) {
                report("老页面自动完善错误 $page ")
                null
            } else page
        } else {
            null
        }
    }

    // 新页面，h5Url,pageName 都是必须的
    private fun prepareNewPage(p: WxPage): WxPage? {
        var page = p
        page.jsVersion = MAX_VERSION
        page.appVersion = MIN_VERSION
        // page.h5Url = page.h5Url ?: validOldPage.h5Url
        // page.remoteJs = page.remoteJs ?: debugWeexPageMaker(page, mHost)
        page.md5 = ""
        page = CubeWx.mWxDebugAdapter.completeDebugWeexPage(page, mDebugHost)
        return if (page.h5Url.isNullOrBlank() || page.remoteJs.isNullOrBlank()) {
            report("新页面自动完善错误 $page ")
            null
        } else page
    }

    private fun updateWeexPageMap(pages: List<WxPage>) {
        mWeexPageMap.isNotEmpty().let { mWeexPageMap.clear() }
        pages.forEach {
            it.h5Url?.let { url ->
                mWeexPageMap[UrlKey.fromUrl(url)] = it
            }
        }
        CubeWx.mWxRouter.mInterceptor = { url ->
            if (url.indexOf("/") == -1) {
                // 通过 pageName 查找
                mWeexPageMap.values.firstOrNull {
                    it.pageName == url
                }
            } else {
                // 通过 url 查找
                mWeexPageMap[UrlKey.fromUrl(url)]
            }
        }
    }

    internal fun autoJump(context: Context) {
        mDebugWeexPagesResp?.autoJumpPage?.let {
            CubeWx.mWxRouter.openUrl(context, it)
        }
    }


    private fun checkDebug(): Boolean {
        mDebugEnable = DiskKVManager.getInst().get(DEBUG_ENABLE, false)
        mDebugHost = DiskKVManager.getInst().get(DEBUG_HOST, "")
        if (!mDebugEnable || mDebugHost.isBlank()) {
            report("未开启调试 $mDebugHost $mDebugEnable")
            return false
        }
        return true
    }
}
