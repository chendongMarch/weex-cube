package com.march.wxcube.debug

import com.march.wxcube.model.WeexPage

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
internal class DebugWeexPagesResp {
    var global = false // 全局调试
    var datas: List<WeexPage> = listOf()
}