package com.march.wxcube.func.update

import android.content.Context
import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
internal interface OnWxUpdateListener {
    fun onWeexCfgUpdate(context: Context, weexPages: List<WxPage>?)
}