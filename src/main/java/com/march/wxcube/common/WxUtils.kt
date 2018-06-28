package com.march.wxcube.common

import com.march.common.utils.DimensUtils

/**
 * CreateAt : 2018/6/20
 * Describe :
 *
 * @author chendong
 */
object WxUtils {

    fun getWxPxByRealPx(px: Int): Float {
        val ratio = 750f / DimensUtils.WIDTH
        return px * ratio
    }
}