package com.march.wxcube.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup

import com.march.common.utils.LgUtils
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.ui.WxActivity
import com.march.wxcube.ui.WxDelegate
import com.taobao.weex.WXSDKManager
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/3/28
 * Describe : Module 扩展
 *
 * @author chendong
 */

// 获取 mCtx
val WXModule.mCtx: Context?
    get() = if (mWXSDKInstance != null) {
        mWXSDKInstance.context
    } else null


// 获取 mAct
val WXModule.mAct: AppCompatActivity?
    get() {
        if (mWXSDKInstance != null) {
            val context = mWXSDKInstance.context
            if (context is AppCompatActivity) {
                return context
            }
        }
        return null
    }


// 获取 mInstId
val WXModule.mInstId: String?
    get() {
        if (mWXSDKInstance != null) {
            return mWXSDKInstance.instanceId
        }
        return null
    }

// 获取 mWeexAct
val WXModule.mWeexAct: WxActivity?
    get() {
        if (mWXSDKInstance != null) {
            val context = mWXSDKInstance.context
            if (context is WxActivity) {
                return context
            }
        }
        return null
    }

// 获取 mDelegate
val WXModule.mWeexDelegate: WxDelegate?
    get() {
//        (WXSDKManager.getInstance()?.wxRenderManager?.allInstances?.firstOrNull {
//            mWXSDKInstance.instanceId == it.instanceId
//        }?.context as? WxActivity)?.mDelegate
//        WXSDKManager.getInstance()?.wxRenderManager?.allInstances?.firstOrNull {
//            mWXSDKInstance.instanceId == it.instanceId
//        }?.
        return ManagerRegistry.WxInst.findWeexDelegateByInstanceId(mWXSDKInstance.instanceId)
    }

// 获取 查找 view
fun WXModule.findView(f: (View) -> Boolean): View? {
    val containerView: ViewGroup = mWXSDKInstance.containerView as ViewGroup
    return findView(containerView, f)
}

private fun WXModule.findView(viewGroup: ViewGroup, f: (View) -> Boolean): View? {
    var view: View? = null
//        LgUtils.e("开始遍历一个 ViewGroup 共有 ${viewGroup.childCount} 个孩子")
    for (i in (0 until viewGroup.childCount)) {
        view = viewGroup.getChildAt(i)
//            LgUtils.e("第${i}个孩子$view  ${view.tag}")
        val result = f.invoke(view)
        if (result) {
            LgUtils.e("监测到，返回数据")
            return view
        } else if (view is ViewGroup) {
            view = findView(view, f)
        }
    }
    return view
}

