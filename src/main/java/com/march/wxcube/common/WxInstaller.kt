package com.march.wxcube.common

import com.alibaba.android.bindingx.plugin.weex.BindingX
import com.march.common.utils.FileUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.module.OneModule
import com.march.wxcube.widget.Container
import com.taobao.weex.WXSDKEngine
import com.taobao.weex.common.WXException
import java.io.File

/**
 * CreateAt : 2018/6/27
 * Describe :
 *
 * @author chendong
 */
object WxInstaller {

    // 注册 bindingX
    fun registerBindingX() {
        try {
            BindingX.register()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 注册 Compnent
    fun registerComponent() {
        try {
            WXSDKEngine.registerComponent("container", Container::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 注册 module
    fun registerModule() {
        try {
            WXSDKEngine.registerModule("bridge", OneModule::class.java, true)
        } catch (e: WXException) {
            e.printStackTrace()
        }
    }




}