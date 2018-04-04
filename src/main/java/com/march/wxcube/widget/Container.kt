package com.march.wxcube.widget

import android.content.Context
import android.widget.FrameLayout
import com.march.wxcube.R
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.dom.WXDomObject
import com.taobao.weex.ui.component.WXComponent
import com.taobao.weex.ui.component.WXVContainer

/**
 * CreateAt : 2018/4/3
 * Describe :
 *
 * @author chendong
 */
class Container : WXComponent<FrameLayout> {

    private val hostView: FrameLayout by lazy {
        val view = FrameLayout(context)
        view.id = R.id.fragment_container_id
        view.tag = "container"
        view
    }

    constructor(instance: WXSDKInstance?, dom: WXDomObject?, parent: WXVContainer<*>?) : super(instance, dom, parent) {
        install()
    }

    constructor(instance: WXSDKInstance?, dom: WXDomObject?, parent: WXVContainer<*>?, type: Int) : super(instance, dom, parent, type) {
        install()
    }

    private fun install() {

    }

    override fun initComponentHostView(context: Context): FrameLayout {
        return hostView
    }
}