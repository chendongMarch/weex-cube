package com.march.wxcube.manager

import com.taobao.weex.WXSDKInstance


/**
 * CreateAt : 2018/4/4
 * Describe :
 *
 * @author chendong
 */
open class BaseManager {
    open fun onViewCreated() {}

    open fun onDestroy(instance: WXSDKInstance) {}
}