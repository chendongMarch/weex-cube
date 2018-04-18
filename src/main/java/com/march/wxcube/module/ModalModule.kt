package com.march.wxcube.module

import com.march.common.utils.ToastUtils
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.ui.module.WXModalUIModule

/**
 * CreateAt : 2018/4/18
 * Describe :
 *
 * @author chendong
 */
class ModalModule : WXModalUIModule() {

    @JSMethod(uiThread = true)
    fun toast(msg: String) {
        ToastUtils.show(msg)
    }

    @JSMethod(uiThread = true)
    fun toastLong(msg: String) {
        ToastUtils.showLong(msg)
    }
}