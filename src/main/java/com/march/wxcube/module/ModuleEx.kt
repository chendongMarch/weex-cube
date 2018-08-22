package com.march.wxcube.module

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup

import com.march.common.utils.LgUtils
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.ui.WxActivity
import com.march.wxcube.ui.WxDelegate
import com.taobao.weex.common.WXModule

/**
 * CreateAt : 2018/3/28
 * Describe : Module 扩展
 *
 * @author chendong
 */

fun Any?.ignore() {
}

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
    val views = mutableListOf<View>()
    findView(containerView, views)
    return views.first(f)
}

private fun WXModule.findView(viewGroup: ViewGroup, list: MutableList<View>) {
    for (i in (0 until viewGroup.childCount)) {
        val child = viewGroup.getChildAt(i)
        list.add(child)
        if (child is ViewGroup) {
            findView(child, list)
        }
    }
}

