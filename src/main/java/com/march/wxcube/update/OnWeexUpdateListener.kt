package com.march.wxcube.update

import android.content.Context
import com.march.wxcube.model.WeexPage

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
internal interface OnWeexUpdateListener {
    fun onWeexCfgUpdate(context: Context, weexPages: List<WeexPage>?)
}