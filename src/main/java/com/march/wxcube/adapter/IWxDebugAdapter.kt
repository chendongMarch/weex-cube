package com.march.wxcube.adapter

import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/27
 * Describe : 调试
 *
 * @author chendong
 */
interface IWxDebugAdapter {
    /**
     * 完善调试配置
     */
    fun completeDebugWeexPage(page: WxPage, host: String): WxPage = page

    /**
     * 构造调试配置请求文件地址
     */
    fun makeDebugConfigUrl(host: String): String = ""

}

open class DefaultWxDebugAdapter : IWxDebugAdapter