package com.march.wxcube.wxadapter

import android.content.Context

import com.taobao.weex.adapter.IWXUserTrackAdapter
import com.taobao.weex.common.WXPerformance

import java.io.Serializable

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class UtAdapter : IWXUserTrackAdapter {
    override fun commit(context: Context, eventId: String, type: String, perf: WXPerformance, params: Map<String, Serializable>) {

    }
}

