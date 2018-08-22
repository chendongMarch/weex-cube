package com.march.wxcube.module.dispatcher

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.march.common.extensions.BarUI
import com.march.wxcube.module.*
import com.taobao.weex.ui.view.WXEditText

/**
 * CreateAt : 2018/6/7
 * Describe : android 特有方法支持
 *
 * @author chendong
 */
class AndroidDispatcher(val module: BridgeModule) : BaseDispatcher() {

    @DispatcherJsMethod
    fun getEditTextContent(args: WxArgs) {
        val view = module.findView {
            it is WXEditText
        }
        var result = ""
        if (view != null && view is EditText) {
            result = view.text.toString().trim()
        }
        args.callback(mapOf(KEY_SUCCESS to true, KEY_RESULT to mapOf(KEY_DATA to result)))
    }

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
    fun hideKeyboard(args: WxArgs) {
        val imm = mProvider.activity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mProvider.activity().currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}