package com.march.wxcube.module.dispatcher

import android.app.Activity
import com.alibaba.fastjson.JSONObject
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class StatusBarDispatcher : BaseDispatcher() {

    companion object {
        const val translucentStatusBar = "translucentStatusBar"
        const val setStatusBarLight = "setStatusBarLight"
        const val setStatusBarDark = "setStatusBarDark"
    }


    override fun getMethods(): Array<String> {
        return arrayOf(translucentStatusBar, setStatusBarDark, setStatusBarLight)
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val act = findAct()
        when (method) {
            translucentStatusBar -> translucentStatusBar(act)
            setStatusBarDark     -> setStatusBarDark(act)
            setStatusBarLight    -> setStatusBarLight(act)
        }
    }


    /**
     * 状态栏透明，必须在 create 中调用，否则不生效
     */
    private fun translucentStatusBar(act: Activity) {
        ImmersionStatusBarUtils.translucent(act)
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