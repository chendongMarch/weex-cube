package com.march.wxcube.adapter

import com.march.common.utils.LgUtils

/**
 * CreateAt : 2018/6/27
 * Describe : 上报
 *
 * @author chendong
 */
interface IWxReportAdapter {
    /**
     * 错误打印
     */
    fun reportError(throwable: Throwable?, errorMsg: String)

    /**
     * log 打印
     */
    fun log(tag: String, msg: String)

}

class DefaultWxReportAdapter : IWxReportAdapter {
    override fun reportError(throwable: Throwable?, errorMsg: String) {
        LgUtils.e("wx-error", errorMsg)
        throwable?.let { LgUtils.e("wx-error", it) }
    }

    override fun log(tag: String, msg: String) {
        LgUtils.e(tag, msg)
    }
}