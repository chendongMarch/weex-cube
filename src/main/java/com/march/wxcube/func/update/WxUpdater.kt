package com.march.wxcube.func.update

import android.content.Context
import com.march.wxcube.CubeWx
import com.march.wxcube.common.JsonSyncMgr
import com.march.wxcube.common.WxUtils
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/4/21
 * Describe : weex 配置更新管理
 * @author chendong
 */
class WxUpdater {

    companion object {
        const val KEY = "wx-config"
    }

    private val jsonSyncMgr by lazy {
        val cfg = JsonSyncMgr.SyncCfg(KEY, CubeWx.mWxCfg.wxCfgUrl)
        JsonSyncMgr(cfg) { weakCtx, json ->
            parseJsonAndUpdate(weakCtx.get(), json)
        }
    }

    // 解析配置文件，并通知出去
    private fun parseJsonAndUpdate(context: Context?, json: String?): Boolean {
        if (json == null || json.isBlank()) {
            return false
        }
        if(context == null) {
            return false
        }
        try {
            val wxPageResp = CubeWx.mWxModelAdapter.convert(json)
            val wxPages = wxPageResp?.datas ?: return false
            // 过滤数据
            val filterPages = if (needPrepare) PageFilter.filter(context, wxPages) {
                CubeWx.mWxJsLoader.prepareRemoteJsSync(context, it)
            } else PageFilter.filter(context, wxPages) {
                if (it.isNotEmpty() && CubeWx.mWxCfg.fortest) {
                    val pageStr = it.joinToString { it.pageName ?: "no page" }
                    CubeWx.mWxReportAdapter.toast(context, "第二次准备发现未下载成功页面，请检查\n $pageStr", true)
                }
            }
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

    private var needPrepare = false

    fun update(context: Context, needPrepare: Boolean = true) {
        this.needPrepare = needPrepare
        this.jsonSyncMgr.update(context)
    }

}
