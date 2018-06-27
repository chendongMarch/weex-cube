package com.march.wxcube.manager

import com.march.wxcube.model.WxPage
import com.march.wxcube.ui.WeexDelegate
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
interface IManager {
    fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?)
    fun onWxInstInit(weexPage: WxPage?, instance: WXSDKInstance?, weexDelegate: WeexDelegate?) {}
}


interface ManagerFactory {
    fun build(): IManager
}