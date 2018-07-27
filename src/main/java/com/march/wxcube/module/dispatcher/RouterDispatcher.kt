package com.march.wxcube.module.dispatcher

import android.content.Intent
import com.march.wxcube.CubeWx
import com.march.wxcube.R
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.getDef
import com.march.wxcube.common.toObjEx
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.module.DispatcherJsMethod
import com.march.wxcube.module.DispatcherParam
import com.march.wxcube.ui.WxActivity
import com.taobao.weex.WXSDKManager

/**
 * CreateAt : 2018/6/6
 * Describe : 路由模块分发
 *
 * @author chendong
 */
class RouterDispatcher : BaseDispatcher() {

    @DispatcherJsMethod
    fun openNative(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openNative url is null")
        val clazz = Class.forName(webUrl)
        val activity = mProvider.activity()
        activity.startActivity(Intent(activity, clazz))
    }

    @DispatcherJsMethod
    fun closePage(param: DispatcherParam) {
        mProvider.doBySelf(param.method,  param.params)
    }

    @DispatcherJsMethod
    fun putExtraData(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#putExtraData url is null")
        val data = param.params[KEY_DATA] ?: throw RuntimeException("Router#putExtraData data is null")
        ManagerRegistry.Data.putData(webUrl, data)
    }

    @DispatcherJsMethod
    fun openApp(param: DispatcherParam) {
        openBrowser(param)
    }

    @DispatcherJsMethod
    fun openWeb(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openWeb url is null")
        val result = CubeWx.mWxRouter.openWeb(findAct(), webUrl)
        if (!result.first) {
            throw RuntimeException("Router#openWeb Error ${result.second}")
        }
    }

    @DispatcherJsMethod
    fun openDialog(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openDialog url is null")
        val configJsonObj = param.params.getJSONObject(KEY_CONFIG)
        val config = configJsonObj.toObjEx(DialogConfig::class.java) ?: DialogConfig()
        val result = CubeWx.mWxRouter.openDialog(findAct(), webUrl, config)
        if (!result.first) {
            throw RuntimeException("Router#openDialog Error ${result.second}")
        }
    }

    @DispatcherJsMethod
    fun openBrowser(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openBrowser url is null")
        val result = CubeWx.mWxRouter.openBrowser(findAct(), webUrl)
        if (!result.first) {
            throw RuntimeException("Router#openBrowser Error ${result.second}")
        }
    }

    @DispatcherJsMethod
    fun openHomePage(param: DispatcherParam) {
        try {
            val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openUrl url is null")
            val allInstances = WXSDKManager.getInstance().wxRenderManager.allInstances
            var hasHome = false
            for (inst in allInstances) {
                val wxAct = inst?.context as? WxActivity
                if (wxAct?.mDelegate?.mWxPage?.h5Url?.contains(webUrl) == false) {
                    WxUtils.finishActivity(wxAct)
                } else {
                    hasHome = true
                }
            }
            if (!hasHome) {
                openUrl(param)
            }
        } catch (e: Exception) {
            openUrl(param)
        }
    }

    @DispatcherJsMethod
    fun openUrl(param: DispatcherParam) {
        val webUrl = param.params.getString(KEY_URL) ?: throw RuntimeException("Router#openUrl url is null")
        val anim = param.params.getDef("animation", "normal")
        val result = CubeWx.mWxRouter.openUrl(findAct(), webUrl) {
            it.putExtra("animation", anim)
        }
        mProvider.activity().overridePendingTransition(R.anim.act_bottom_in, R.anim.act_no_anim)
        when (anim) {
            "btc"  -> mProvider.activity().overridePendingTransition(R.anim.act_bottom_in, R.anim.act_no_anim)
            "fade" -> mProvider.activity().overridePendingTransition(R.anim.act_fast_fade_in, R.anim.act_no_anim)
            "rtl"  -> mProvider.activity().overridePendingTransition(R.anim.act_translate_in, R.anim.act_no_anim)
            "no"   -> mProvider.activity().overridePendingTransition(R.anim.act_no_anim, R.anim.act_no_anim)
            else   -> mProvider.activity().overridePendingTransition(R.anim.act_translate_in, R.anim.act_no_anim)
        }
        if (!result.first) {
            throw RuntimeException("Router#openUrl Error ${result.second}")
        }
    }

}