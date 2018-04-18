package com.march.wxcube.module

import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/4/2
 * Describe : 状态栏
 *
 * @author chendong
 */
class StatusBarModule : WXModule() {

    /**
     * 状态栏透明，必须在 create 中调用，否则不生效
     */
    @JSMethod(uiThread = true)
    fun transluteStatusBar() {
        val act = activity ?: return
        ImmersionStatusBarUtils.translucent(act)
    }

    /**
     * 状态栏颜色黑字
     */
    @JSMethod(uiThread = true)
    fun setStatusBarLight() {
        val act = activity ?: return
        ImmersionStatusBarUtils.setStatusBarLightMode(act)
    }

    /**
     * 状态栏颜色白字
     */
    @JSMethod(uiThread = true)
    fun setStatusBarDark() {
        val act = activity ?: return
        ImmersionStatusBarUtils.setStatusBarDarkMode(act)
    }
}