package com.march.wxcube.module.dispatcher

import com.march.common.extensions.BarUI
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
        BarUI.translucent(findAct())
        args.ignore()
    }

    /**
     * 隐藏底部状态栏达到全屏的目的
     */
    @DispatcherJsMethod
    fun hideBottomUI(args: WxArgs) {
        try {
            BarUI.hideBottomBar(findAct())
           args.ignore()
        } catch (e: Exception) {

        }
    }

    /**
     * 状态栏颜色黑字
     */
    @DispatcherJsMethod
    fun setStatusBarLight(args: WxArgs) {
        BarUI.setStatusBarLightMode(findAct())
       args.ignore()
    }

    /**
     * 状态栏颜色白字
     */
    @DispatcherJsMethod
    fun setStatusBarDark(args: WxArgs) {
        BarUI.setStatusBarDarkMode(findAct())
        args.ignore()
    }
}