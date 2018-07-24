package com.march.wxcube.module

import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/12
 * Describe :
 *
 * @author chendong
 */
class Callback(private var jsCallback: JSCallback?) {
    operator fun invoke(obj: Any) {
        jsCallback?.invoke(obj)
        jsCallback = null
    }
}