package com.march.wxcube.ui

import android.app.Activity
import com.march.wxcube.CubeWx
import com.march.wxcube.common.WxConstants
import com.march.wxcube.model.WxPage
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.RenderContainer
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy

/**
 * CreateAt : 2018/5/3
 * Describe :
 *
 * @author chendong
 */
class WxRender(activity: Activity,
               private val mWxInst: WXSDKInstance,
               private val listener: IWXRenderListener) {

    init {
        val renderContainer = RenderContainer(activity)
        mWxInst.setRenderContainer(renderContainer)
        mWxInst.registerRenderListener(listener)
    }

    fun renderJs(page: WxPage, opts: Map<String, Any>, js: String) {
        mWxInst.render(page.pageName, js, opts, null, WXRenderStrategy.APPEND_ASYNC)
    }

    fun render(page: WxPage, opts: Map<String, Any>, consumer: ((String?) -> Unit)? = null) {
        if (!page.isValid) {
            listener.onException(mWxInst, WxConstants.ERR_PAGE_NOT_VALID, "页面数据有问题${page.toShowString()}")
            return
        }
        CubeWx.mWxJsLoader.getTemplateAsync(mWxInst.context, page) {
            if (!it.isNullOrBlank()) {
                consumer?.invoke(it)
                mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else {
                listener.onException(mWxInst, WxConstants.ERR_JS_NOT_READY, "JS NOT READY ${page.toShowString()}")
            }
        }
    }

    fun onDestroy() {
        mWxInst.registerRenderListener(null)
    }

}