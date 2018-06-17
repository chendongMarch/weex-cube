package com.march.wxcube.debug

import com.march.wxcube.common.newLine

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */

class DebugMsg {
    var lastTemplate = ""
    var errorMsg = ""
    var refreshing = false

    fun toShowString(): String {
        return StringBuilder()
                .append("error = ").append(errorMsg).newLine()
                .append(if (refreshing) "刷新中" else "没有刷新").newLine()
                .toString()

    }
}
