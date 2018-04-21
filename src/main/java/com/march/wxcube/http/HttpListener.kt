package com.march.wxcube.http

import com.taobao.weex.adapter.IWXHttpAdapter
import com.taobao.weex.common.WXResponse

/**
 * CreateAt : 2018/4/21
 * Describe :
 *
 * @author chendong
 */
interface HttpListener : IWXHttpAdapter.OnHttpListener {
    override fun onHttpFinish(response: WXResponse) {

    }

    override fun onHttpResponseProgress(loadedLength: Int) {
    }

    override fun onHeadersReceived(statusCode: Int, headers: MutableMap<String, MutableList<String>>?) {
    }

    override fun onHttpStart() {
    }

    override fun onHttpUploadProgress(uploadProgress: Int) {
    }
}