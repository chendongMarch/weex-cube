package com.march.wxcube.module.dispatcher

import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.wxcube.common.Device
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs
import com.march.wxcube.module.ignore

/**
 * CreateAt : 2018/6/7
 * Describe : android 特有方法支持
 *
 * @author chendong
 */
class AndroidDispatcher : BaseDispatcher() {

    /**
     * 状态栏透明，必须在 create 中调用，否则不生效
     */
    @DispatcherJsMethod
    fun translucentStatusBar(args: WxArgs) {
        ImmersionStatusBarUtils.translucent(findAct())
        args.ignore()
    }

    /**
     * 隐藏底部状态栏达到全屏的目的
     */
    @DispatcherJsMethod
    fun hideBottomUI(args: WxArgs) {
        try {
            Device.hideBottomUI(findAct())
           args.ignore()
        } catch (e: Exception) {

        }
    }

    /**
     * 状态栏颜色黑字
     */
    @DispatcherJsMethod
    fun setStatusBarLight(args: WxArgs) {
        ImmersionStatusBarUtils.setStatusBarLightMode(findAct())
       args.ignore()
    }

    /**
     * 状态栏颜色白字
     */
    @DispatcherJsMethod
    fun setStatusBarDark(args: WxArgs) {
        ImmersionStatusBarUtils.setStatusBarDarkMode(findAct())
       args.ignore()
    }
}