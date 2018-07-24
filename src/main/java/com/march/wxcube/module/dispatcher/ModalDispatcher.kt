package com.march.wxcube.module.dispatcher

import com.march.common.utils.ToastUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam

/**
 * CreateAt : 2018/6/6
 * Describe : 模态
 *
 * @author chendong
 */
class ModalDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun toast(param: DispatcherParam) {
        val duration = param.params.getDef(KEY_DURATION, 2)
        val msg = param.params.getDef(KEY_MSG, "no msg")
        if (duration <= 2) {
            ToastUtils.show(msg)
        } else {
            ToastUtils.showLong(msg)
        }
    }

    @DispatcherJsMethod
    fun loading(param: DispatcherParam) {
        mProvider.doBySelf(param.method, param.params)
    }

}