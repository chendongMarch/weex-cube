package com.march.wxcube.module

import com.march.common.utils.ToastUtils
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.ui.module.WXModalUIModule

/**
 * CreateAt : 2018/4/18
 * Describe : 模态，主要是 toast/alert/confirm
 *
 * @author chendong
 */
class ModalModule : WXModalUIModule() {

    /**
     * toast
     */
    @JSMethod(uiThread = true)
    fun toast(msg: String) {
        ToastUtils.show(msg)
    }

    /**
     * toast long
     */
    @JSMethod(uiThread = true)
    fun toastLong(msg: String) {
        ToastUtils.showLong(msg)
    }
}