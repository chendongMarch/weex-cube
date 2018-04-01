package com.march.wxcube.adapter

import com.taobao.weex.appfram.websocket.IWebSocketAdapter
import com.taobao.weex.appfram.websocket.IWebSocketAdapterFactory

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WebSocketAdapter : IWebSocketAdapter {

    class WebSocketFactory : IWebSocketAdapterFactory {
        override fun createWebSocketAdapter(): IWebSocketAdapter {
            return WebSocketAdapter()
        }
    }

    override fun connect(url: String, protocol: String?, listener: IWebSocketAdapter.EventListener) {

    }

    override fun send(data: String) {

    }

    override fun close(code: Int, reason: String) {

    }

    override fun destroy() {

    }

    companion object {

        fun createFactory(): WebSocketFactory {
            return WebSocketFactory()
        }
    }
}
