package com.march.wxcube.ui

import android.app.Activity
import android.text.TextUtils
import android.view.View

import com.march.wxcube.Weex
import com.march.wxcube.model.WeexPage
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.RenderContainer
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy

/**
 * CreateAt : 2018/3/19
 * Describe : weex 渲染管理
 *
 * @author chendong
 */
class WeexRender(activity: Activity, private val mWxInst: WXSDKInstance, private val listener: IWXRenderListener) {

    init {
        val renderContainer = RenderContainer(activity)
        mWxInst.setRenderContainer(renderContainer)
        mWxInst.registerRenderListener(listener)
    }

    fun render(page: WeexPage, opts: Map<String, Any>) {
        if (!page.isValid) {
            listener.onException(mWxInst, "100", "页面数据有问题")
            return
        }
        Weex.getInst().jsBundleCache.getTemplateAsync(mWxInst.context, page) {
            if (!TextUtils.isEmpty(it)) {
                mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else if (!TextUtils.isEmpty(page.remoteJs)) {
                mWxInst.renderByUrl(page.pageName, page.remoteJs, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else {
                Weex.instance.weexService.onErrorReport(null, "render error " + page.toString())
                listener.onException(mWxInst, "101", "页面数据有问题")
            }
        }
    }


}
