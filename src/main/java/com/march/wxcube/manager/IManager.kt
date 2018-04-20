package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
interface IManager {
    fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?)
}


interface ManagerFactory {
    fun build(): IManager
}