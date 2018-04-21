package com.march.wxcube.manager

import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance

/**
 * CreateAt : 2018/4/21
 * Describe :
 *
 * @author chendong
 */
class EnvManager : IManager {

    companion object {
        val instance: EventManager by lazy { EventManager() }
    }


    val mEnvHostMap by lazy { mutableMapOf<Int, String>() }


    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {

    }


}