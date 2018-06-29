package com.march.wxcube.update

import android.content.Context
import com.march.common.Common
import com.march.common.utils.StreamUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.DiskLruCache
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.report
import com.march.wxcube.http.HttpListener
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.manager.RequestManager
import com.march.wxcube.model.WxPage
import com.taobao.weex.common.WXResponse
import java.util.concurrent.Executors

/**
 * CreateAt : 2018/4/21
 * Describe :
 * @author chendong
 */
class WxUpdater(private var url: String) {

    private val mDiskLruCache by lazy {
        DiskLruCache(WxUtils.makeCacheDir(CACHE_DIR), DISK_MAX_SIZE)
    }
    private val mExecutorService by lazy { Executors.newCachedThreadPool() }

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
        mExecutorService.execute {
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
                // 简化和过滤
                val pages = simplifyPages(it) { it.isValid }
                // 完善数据
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


    // 1. 相同页面只保留 jsVersion 较高的一个
    // 2. 页面数据支持的 appVersion 小于等于当前 app
    private fun simplifyPages(pages: List<WxPage>, predicate: (WxPage) -> Boolean = { true }): List<WxPage> {
        val curVersionCodes = getVersionCodes(Common.BuildConfig.VERSION_NAME)
        if (curVersionCodes.size != 3) {
            return pages
        }
        val mutablePages = mutableListOf<WxPage>()
        // 过滤有效的配置，外部传入的规则，版本号为3位，当前版本要>=配置的版本
        pages.filterTo(mutablePages) {
            // 外部传入的过滤
            if (!predicate(it)) return@filterTo false
            val appVersionCodes = getVersionCodes(it.appVersion)
            val jsVersionCodes = getVersionCodes(it.jsVersion)
            if (jsVersionCodes.size != 3 || appVersionCodes.size != 3) return@filterTo false
            // 当前版本要 >= 支持版本
            curVersionCodes[0] >= appVersionCodes[0]
                    && curVersionCodes[1] >= appVersionCodes[1]
                    && curVersionCodes[2] >= appVersionCodes[2]
        }
        val pageNameWeexPageMap = mutableMapOf<String?, WxPage>()
        // 每个页面只保留一个配置
        for (page in mutablePages) {
            val pageName = page.pageName ?: continue
            val value: WxPage? = pageNameWeexPageMap[pageName]
            if (value == null) {
                // 新页面
                pageNameWeexPageMap[pageName] = page
            } else {
                // 比较 jsVersion，如果当前的 js 版本比较大，则选择当前的
                val curJsVersionCodes = getVersionCodes(page.jsVersion)
                val lastJsVersionCodes = getVersionCodes(value.jsVersion)
                if (curJsVersionCodes[0] >= lastJsVersionCodes[0]
                        && curJsVersionCodes[1] >= lastJsVersionCodes[1]
                        && curJsVersionCodes[2] >= lastJsVersionCodes[2]) {
                    pageNameWeexPageMap[pageName] = page
                }
            }
        }
        mutablePages.clear()
        mutablePages.addAll(pageNameWeexPageMap.values)
        return mutablePages
    }

    private fun getVersionCodes(version: String?): List<Int> {
        if (version == null) {
            return listOf()
        }
        val vs = version.split(".")
        return vs.map { it.toInt() }
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