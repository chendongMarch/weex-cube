package com.march.wxcube.module.dispatcher

import com.march.common.utils.LgUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs

/**
 * CreateAt : 2018/6/6
 * Describe : 调试模块分发
 *
 * @author chendong
 */
class DebugDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun debugToast(args: WxArgs) {
        val duration = args.params.getDef(KEY_DURATION, 2)
        val msg = args.params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.showLong(msg)
        } else {
            ToastUtils.show(msg)
        }
    }

    @DispatcherJsMethod
    fun debugLog(args: WxArgs) {
        val tag = args.params.getDef(KEY_TAG, "weex-debug")
        val msg = args.params.getDef(KEY_MSG, "no msg")
        LgUtils.e(tag, msg)
    }
}