package com.march.wxcube.debug

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import android.widget.Button
import com.march.wxcube.JsCacheStrategy
import com.march.wxcube.JsLoadStrategy
import com.march.wxcube.Weex
import com.march.wxcube.common.click
import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.model.WeexPage
import com.march.wxcube.ui.WeexDelegate

/**
 * CreateAt : 2018/5/3
 * Describe :
 *
 * @author chendong
 */
class WeexDebugger(private val mWeexDelegate: WeexDelegate,
                   private val mActivity: Activity,
                   private val mWeexPage: WeexPage,
                   private val mContainerView: ViewGroup):WeexLifeCycle {

    var mLastTemplate: String? = null

    private val mHandler by lazy { Handler(Looper.getMainLooper()) { refresh() } }

    fun addDebugBtn() {
        val button = Button(mContainerView.context)
        button.text = "刷新"
        button.click {
            mHandler.sendEmptyMessage(0)
        }
        mContainerView.addView(button, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun refresh(): Boolean {
        return with(mWeexDelegate) {
            Weex.getInst().mWeexJsLoader.getTemplateAsync(mActivity,
                    JsLoadStrategy.NET_FIRST,
                    JsCacheStrategy.NO_CACHE, mWeexPage) {
                it?.let {
                    if (mLastTemplate == null || !mLastTemplate.equals(it)) {
                        mActivity.runOnUiThread {
                            onDestroy()
                            init(mActivity)
                            onCreate()
                            render()
                        }
                    } else {
                        Weex.getInst().mWeexInjector.onLog("refresh", "获取到但是没有改变，不作渲染")
                    }
                }
                mHandler.sendEmptyMessageDelayed(0, 2000)
            }
            true
        }
    }

    override fun onDestroy() {
        mLastTemplate = null
        mHandler.removeCallbacksAndMessages(null)
    }
}