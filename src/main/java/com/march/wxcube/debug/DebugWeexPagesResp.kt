package com.march.wxcube.debug

import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
internal class DebugWeexPagesResp {
    var global = false // 全局调试
    var autoJumpPage: String = ""
    var datas: List<WxPage> = listOf()
}