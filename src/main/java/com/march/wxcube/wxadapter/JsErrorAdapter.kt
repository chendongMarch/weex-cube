package com.march.wxcube.wxadapter

import com.march.wxcube.common.report
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
          report(exception.toString())
    }
}
