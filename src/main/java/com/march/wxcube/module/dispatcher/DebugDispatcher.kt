package com.march.wxcube.module.dispatcher

import com.march.common.utils.LgUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam

/**
 * CreateAt : 2018/6/6
 * Describe : 调试模块分发
 *
 * @author chendong
 */
class DebugDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun toast(param: DispatcherParam) {
        val duration = param.params.getDef(KEY_DURATION, 2)
        val msg = param.params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.showLong(msg)
        } else {
            ToastUtils.show(msg)
        }
    }

    @DispatcherJsMethod
    fun log(param: DispatcherParam) {
        val tag = param.params.getDef(KEY_TAG, "weex-debug")
        val msg = param.params.getDef(KEY_MSG, "no msg")
        LgUtils.e(tag, msg)
    }
}