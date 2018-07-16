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
        JsonSyncMgr(cfg) { ctx, json ->
            parseJsonAndUpdate(ctx, json)
        }
    }

    // 解析配置文件，并通知出去
    private fun parseJsonAndUpdate(context: Context, json: String?): Boolean {
        if (json == null || json.isBlank()) {
            return false
        }
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

    fun update(context: Context) {
        jsonSyncMgr.update(context)
    }

}
