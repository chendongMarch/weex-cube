package com.march.wxcube.module.dispatcher

import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs

/**
 * CreateAt : 2018/6/6
 * Describe : 模态
 *
 * @author chendong
 */
class ModalDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun toast(args: WxArgs) {
        val duration = args.params.getDef(KEY_DURATION, 2)
        val msg = args.params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.show(msg)
        } else {
            ToastUtils.showLong(msg)
        }
    }

    @DispatcherJsMethod
    fun loading(args: WxArgs) {
        mProvider.doBySelf(args.method, args.params)
    }

}