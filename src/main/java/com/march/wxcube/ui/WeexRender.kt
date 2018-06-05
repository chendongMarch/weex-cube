package com.march.wxcube.ui

import android.app.Activity
import com.march.common.utils.LogUtils
import com.march.wxcube.Weex
import com.march.wxcube.common.report
import com.march.wxcube.model.WeexPage
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
class WeexRender(activity: Activity,
                 private val mWxInst: WXSDKInstance,
                 private val listener: IWXRenderListener) {


    init {
        val renderContainer = RenderContainer(activity)
        mWxInst.setRenderContainer(renderContainer)
        mWxInst.registerRenderListener(listener)
    }

    fun renderJs(page: WeexPage, opts: Map<String, Any>, js: String) {
        mWxInst.render(page.pageName, js, opts, null, WXRenderStrategy.APPEND_ASYNC)
    }

    fun render(page: WeexPage, opts: Map<String, Any>, consumer: ((String?) -> Unit)? = null) {
        if (!page.isValid) {
            listener.onException(mWxInst, "100", "页面数据有问题")
            return
        }
        Weex.getInst().mWeexJsLoader.getTemplateAsync(mWxInst.context, page) {
            LogUtils.e("render thread = ${Thread.currentThread().name}")
            if (!it.isNullOrBlank()) {
                consumer?.invoke(it)
                mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else {
                report("render error " + page.toString())
                listener.onException(mWxInst, "101", "js 没有准备好")
            }
        }
    }

    fun onDestroy() {
        mWxInst.registerRenderListener(null)
    }

}