package com.march.wxcube.ui

import android.app.Activity
import com.march.wxcube.CubeWx
import com.march.wxcube.common.WxConstants
import com.march.wxcube.model.WxPage
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.RenderContainer
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

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

    fun render(page: WxPage, opts: Map<String, Any>, renderJsCallback: ((String?) -> Unit)? = null) {
        if (!page.isValid) {
            listener.onException(mWxInst, WxConstants.ERR_PAGE_NOT_VALID, "页面数据有问题${page.toShowString()}")
            return
        }
        if(mWxInst.context == null) {
            return
        }
//        CubeWx.mWxJsLoader.getTemplateAsync(mWxInst.context, page) {
//            if (!it.isNullOrBlank()) {
//                renderJsCallback?.invoke(it)
//                mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
//            } else {
//                listener.onException(mWxInst, WxConstants.ERR_JS_NOT_READY, "JS NOT READY ${page.toShowString()}")
//            }
//        }

        launch(UI) {
            val deferred = CubeWx.mWxJsLoader.getTemplateCoroutine(mWxInst.context, page)
            val template = deferred?.await()
            if (!template.isNullOrBlank()) {
                renderJsCallback?.invoke(template)
                mWxInst.render(page.pageName, template, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else {
                listener.onException(mWxInst, WxConstants.ERR_JS_NOT_READY, "JS NOT READY ${page.toShowString()}")
            }
        }
    }

    fun onDestroy() {
        mWxInst.registerRenderListener(null)
    }

}