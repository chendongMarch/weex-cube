package com.march.wxcube.wxadapter

import com.march.wxcube.CubeWx
import com.march.wxcube.adapter.IWxReportAdapter
import com.taobao.weex.adapter.IWXJSExceptionAdapter
import com.taobao.weex.common.WXJSExceptionInfo

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class JsErrorAdapter : IWXJSExceptionAdapter {
    override fun onJSException(exception: WXJSExceptionInfo) {
        CubeWx.mWxReportAdapter.report(IWxReportAdapter.CODE_JS_EXCEPTION, exception.toString())
    }
}
