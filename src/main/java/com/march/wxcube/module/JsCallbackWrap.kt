package com.march.wxcube.module

import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/12
 * Describe :
 *
 * @author chendong
 */
class JsCallbackWrap(private var jsCallback: JSCallback?) {
    fun invoke(obj: Any) {
        jsCallback?.invoke(obj)
        jsCallback = null
    }
}