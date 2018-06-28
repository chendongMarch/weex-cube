package com.march.wxcube.adapter

import android.app.Activity
import android.view.WindowManager
import com.march.common.utils.immersion.ImmersionStatusBarUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.loading.Loading
import com.march.wxcube.loading.SimpleLoading

/**
 * CreateAt : 2018/6/27
 * Describe : 页面
 *
 * @author chendong
 */
interface IWxPageAdapter {

    /**
     * 内置 Activity 创建时调用
     * 默认行为 weex 页面透明状态栏
     * web 页面全屏显示
     */
    fun onPageCreated(activity: Activity, type: Int)

    /**
     * loading
     * 容器渲染时支持蒙版
     * 使用内置首页的自定义界面
     */
    fun getLoading(): Loading

}


open class DefaultWxPageAdapter : IWxPageAdapter {

    override fun onPageCreated(activity: Activity, type: Int) {
        ImmersionStatusBarUtils.setStatusBarLightMode(activity)
        if (type == CubeWx.PAGE_WEEX || type == CubeWx.PAGE_INDEX) {
            ImmersionStatusBarUtils.translucent(activity)
        } else if (type == CubeWx.PAGE_WEB) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun getLoading(): Loading = SimpleLoading()
}