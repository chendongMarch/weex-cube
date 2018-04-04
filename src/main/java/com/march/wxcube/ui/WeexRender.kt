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
class WeexRender(activity: Activity, private val mWxInst: WXSDKInstance, private val mRenderService: RenderService) {

    init {
        val renderContainer = RenderContainer(activity)
        mWxInst.setRenderContainer(renderContainer)
        mWxInst.registerRenderListener(RenderListener())
    }

    interface RenderService {
        fun onViewCreated(view: View)
    }

    fun render(page: WeexPage, opts: Map<String, Any>) {
        Weex.getInst().jsBundleCache.getTemplateAsync(mWxInst.context, page) {
            if (!TextUtils.isEmpty(it)) {
                mWxInst.render(page.pageName, it, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else if (!TextUtils.isEmpty(page.remoteJs)) {
                mWxInst.renderByUrl(page.pageName, page.remoteJs, opts, null, WXRenderStrategy.APPEND_ASYNC)
            } else {
                Weex.instance.weexService.onErrorReport(null, "render error " + page.toString())
            }
        }
    }


    private inner class RenderListener : IWXRenderListener {

        override fun onViewCreated(instance: WXSDKInstance, view: View) {
            mRenderService.onViewCreated(view)
        }

        override fun onRenderSuccess(instance: WXSDKInstance, width: Int, height: Int) {

        }

        override fun onRefreshSuccess(instance: WXSDKInstance, width: Int, height: Int) {

        }

        override fun onException(instance: WXSDKInstance, errCode: String, msg: String) {
            Weex.instance.weexService.onErrorReport(null, "code = $errCode, msg = $msg")
        }
    }
}
