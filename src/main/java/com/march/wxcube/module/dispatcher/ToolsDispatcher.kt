package com.march.wxcube.module.dispatcher

import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.WxArgs
import com.march.wxcube.module.ignore

/**
 * CreateAt : 2018/6/6
 * Describe : 工具模块分发
 *
 * @author chendong
 */
class ToolsDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun clearCookies(args: WxArgs) {
        ManagerRegistry.Request.getCookieJar().clear()
        args.ignore()
    }

}