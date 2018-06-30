package com.march.wxcube.debug

import com.march.common.pool.DiskKVManager
import com.march.wxcube.CubeWx
import com.march.wxcube.common.newLine
import com.march.wxcube.loader.JsLoadStrategy

/**
 * CreateAt : 2018/6/30
 * Describe :
 *
 * @author chendong
 */

// 全局参数配置
class GlobalWxDebugCfg {
    // 本地 IP 自动刷新
    var isAutoRefreshLocalIp = false
    // 多页面调试 host
    var multiPageDebugHost = ""
    // 多页面调试开关
    var multiPageDebugEnable = false
    // 强制使用远程 js
    var isForceNetJs
        set(value) {
            if (value) {
                CubeWx.mWxCfg.jsLoadStrategy = JsLoadStrategy.NET_FIRST
            } else {
                CubeWx.mWxCfg.jsLoadStrategy = JsLoadStrategy.DEFAULT
            }
        }
        get() = CubeWx.mWxCfg.jsLoadStrategy == JsLoadStrategy.NET_FIRST

    fun flush() {
        DiskKVManager.getInst().put(this::class.java.simpleName, this)
    }

    companion object {
        fun backup(): GlobalWxDebugCfg {
            return DiskKVManager.getInst().getObj(GlobalWxDebugCfg::class.java.simpleName, GlobalWxDebugCfg::class.java, GlobalWxDebugCfg())
        }
    }
}

// 页面调试参数配置
class PageWxDebugCfg {

    // 是否正在刷新
    var isRefreshing = false
    // 正在渲染的模板
    var renderTemplate = ""
    // 错误信息
    var errorMsg = ""

    fun toShowString(): String {
        return StringBuilder()
                .append("error = ").append(errorMsg).newLine()
                .append(if (isRefreshing) "刷新中" else "没有刷新").newLine()
                .toString()
    }
}