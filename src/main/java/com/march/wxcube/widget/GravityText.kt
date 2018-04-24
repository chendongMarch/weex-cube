package com.march.wxcube.widget

import com.taobao.weex.WXSDKInstance
import com.taobao.weex.dom.WXDomObject
import com.taobao.weex.ui.component.WXComponentProp
import com.taobao.weex.ui.component.WXText
import com.taobao.weex.ui.component.WXVContainer

/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
class GravityText : WXText {

    constructor(instance: WXSDKInstance?, dom: WXDomObject?, parent: WXVContainer<*>?) : super(instance, dom, parent)


    @WXComponentProp(name = "gravity")
    fun gravity(gravity: String) {

        when (gravity) {
            "center" -> hostView
        }
    }
}