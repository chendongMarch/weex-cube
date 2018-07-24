package com.march.wxcube.module.dispatcher

import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam
import com.march.wxcube.module.ignore

/**
 * CreateAt : 2018/6/6
 * Describe : 工具模块分发
 *
 * @author chendong
 */
class ToolsDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun clearCookies(param: DispatcherParam) {
        ManagerRegistry.Request.getCookieJar().clear()
        param.ignore()
    }

}