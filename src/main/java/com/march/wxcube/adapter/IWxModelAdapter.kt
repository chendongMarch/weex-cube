package com.march.wxcube.adapter

import com.alibaba.fastjson.JSON
import com.march.wxcube.update.WxPagesResp

/**
 * CreateAt : 2018/6/27
 * Describe : 用于自定义数据结构和容器内数据结构转换
 *
 * @author chendong
 */
interface IWxModelAdapter {
    /**
     * 数据转换
     */
    fun convert(json: String): WxPagesResp?
}

open class DefaultWxModelAdapter : IWxModelAdapter {

    override fun convert(json: String): WxPagesResp? {
        if (json.isEmpty()) {
            return null
        }
        return try {
            JSON.parseObject(json, WxPagesResp::class.java)
        } catch (e: Exception) {
            null
        }
    }
}