package com.march.wxcube.wxadapter

import com.march.wxcube.manager.ManagerRegistry
import com.taobao.weex.adapter.IWXHttpAdapter
import com.taobao.weex.common.WXRequest

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class OkHttpAdapter : IWXHttpAdapter {

    override fun sendRequest(request: WXRequest, listener: IWXHttpAdapter.OnHttpListener) {
        ManagerRegistry.Request.request(request, true, listener)
    }
}
