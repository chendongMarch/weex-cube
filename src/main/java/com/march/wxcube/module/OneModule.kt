package com.march.wxcube.module

import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import com.march.wxcube.common.getDef
import com.march.wxcube.module.dispatcher.*
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.bridge.JSCallback
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/6/5
 * Describe : 单 module 实现
 *
 * @author chendong
 */
class OneModule : WXModule() {

    private val mDispatcherRegistry by lazy {
        DispatcherRegistry(
                OneModuleBridgeProvider(),
                RouterDispatcher(),
                DebugDispatcher(),
                ModalDispatcher(),
                EventDispatcher(this),
                ToolsDispatcher(),
                StatusBarDispatcher(),
                PageDispatcher(this),
                *Weex.mWeexInjector.getModuleDispatchers()
        )
    }

    /**
     * 优点：
     * 1. 避免版本不同造成的方法不兼容
     * 2. 更好的兼容客户端和H5，不会因为某个方法没有造成渲染失败
     * 3. 方法参数回调统一规范性更好
     * 4. 方法调用失败时，可以从 msg 快速定位问题
     * 5. 更好的扩展性，由于统一了参数，扩展 params 也变得简单啦
     * 缺点：
     * 1. 写起来比较繁琐
     * 2. 调用起来不好识别，最好能有 vue 中间层的支持
     *
     * OneModule$call()
     * #method 调用方法的唯一标识
     * #params 传递的参数
     * #callback 结果回调，必须回调回去
     *      success: boolean
     *      msg: string
     */
    @JSMethod(uiThread = true)
    fun call(method: String, params: JSONObject, callback: JSCallback) {
        try {
            mDispatcherRegistry.dispatch(method, params,JsCallbackWrap(callback))
            // mDispatcherRegistry.postJsResult(callback, true to "$method($params) finish ")
        } catch (e: Exception) {
            e.printStackTrace()
            mDispatcherRegistry.postJsResult(callback, false to "$method($params) error ${e.message}")
        }
    }

    inner class OneModuleBridgeProvider : BaseDispatcher.Provider {

        val module = this@OneModule

        override fun provideActivity(): AppCompatActivity {
            return module.mAct?:throw RuntimeException("activity find error")
        }

        override fun doBySelf(method: String, params: JSONObject) {
            when (method) {
                RouterDispatcher.closePage -> {
                    val delegate = module.mWeexDelegate ?: throw RuntimeException("Router#closePage delegate is null")
                    delegate.close()
                }
                ModalDispatcher.loading    -> {
                    // val msg = params.getDef(BaseDispatcher.KEY_MSG, "")
                    val show = params.getDef("show", false)
                    val weexAct = module.mWeexAct ?: throw RuntimeException("ModuleDispatcher#mWeexAct error")
                    val parentView = weexAct.mDelegate.mContainerView
                    val progressView = weexAct.mProgressBar
                    val notLoading = parentView.indexOfChild(progressView) == -1
                    if (show && notLoading) {
                        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        layoutParams.gravity = Gravity.CENTER
                        parentView.addView(progressView, layoutParams)
                        progressView.visibility = View.VISIBLE
                    } else if (!show && !notLoading) {
                        progressView.visibility = View.GONE
                        parentView.removeView(progressView)
                    }
                }
            }
        }
    }
}