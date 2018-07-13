package com.march.wxcube.module.dispatcher

import android.app.Activity
import com.alibaba.fastjson.JSONObject
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.wxcube.common.Device
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class AndroidDispatcher : BaseDispatcher() {

    companion object {
        const val translucentStatusBar = "translucentStatusBar"
        const val setStatusBarLight = "setStatusBarLight"
        const val setStatusBarDark = "setStatusBarDark"
        const val hideBottomUI = "hideBottomUI"
    }


    override fun getMethods(): Array<String> {
        return arrayOf(translucentStatusBar,
                setStatusBarDark,
                setStatusBarLight,
                hideBottomUI)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val act = findAct()
        when (method) {
            translucentStatusBar -> translucentStatusBar(act)
            setStatusBarDark     -> setStatusBarDark(act)
            setStatusBarLight    -> setStatusBarLight(act)
            hideBottomUI         -> hideBottomUI(act)
        }
    }


    /**
     * 状态栏透明，必须在 create 中调用，否则不生效
     */
    private fun translucentStatusBar(act: Activity) {
        ImmersionStatusBarUtils.translucent(act)
    }

    private fun hideBottomUI(act: Activity) {
        try {
            Device.hideBottomUI(act)
        } catch (e: Exception) {

        }
    }

    /**
     * 状态栏颜色黑字
     */
    private fun setStatusBarLight(act: Activity) {
        ImmersionStatusBarUtils.setStatusBarLightMode(act)
    }

    /**
     * 状态栏颜色白字
     */
    private fun setStatusBarDark(act: Activity) {
        ImmersionStatusBarUtils.setStatusBarDarkMode(act)
    }
}