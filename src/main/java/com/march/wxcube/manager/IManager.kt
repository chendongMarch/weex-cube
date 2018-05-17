package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDelegate
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
interface IManager {
    fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?)
    fun onWxInstInit(weexPage: WeexPage?, instance: WXSDKInstance?, weexDelegate: WeexDelegate?) {}
}


interface ManagerFactory {
    fun build(): IManager
}