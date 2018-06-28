package com.march.wxcube.module.dispatcher

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.CubeWx
import com.march.wxcube.common.toObjEx
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/6
 * Describe : 路由模块分发
 *
 * @author chendong
 */
class RouterDispatcher : BaseDispatcher() {

    companion object {
        // method
        const val openUrl = "openUrl"
        const val openNative = "openNative"
        const val openWeb = "openWeb"
        const val openDialog = "openDialog"
        const val openBrowser = "openBrowser"
        const val openApp = "openApp"
        const val closePage = "closePage"
        const val putExtraData = "putExtraData"
    }

    override fun getMethods(): Array<String> {
        return arrayOf(
                openUrl,
                openWeb,
                openDialog,
                openBrowser,
                openApp,
                closePage,
                putExtraData,
                openNative
        )
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val act = findAct()
        when (method) {
            openUrl      -> openUrl(act, params)
            openWeb      -> openWeb(act, params)
            openDialog   -> openDialog(act, params)
            openBrowser  -> openBrowser(act, params)
            openApp      -> openApp(act, params)
            closePage    -> closePage(params)
            putExtraData -> putExtraData(params)
            openNative   -> openNative(params)
        }
    }

    private fun openNative(params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openNative url is null")
        val clazz = Class.forName(webUrl)
        val activity = mProvider.activity()
        activity.startActivity(Intent(activity, clazz))
    }

    private fun closePage(params: JSONObject) {
        mProvider.doBySelf(closePage, params)
    }

    private fun putExtraData(params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#putExtraData url is null")
        val data = params[KEY_DATA] ?: throw RuntimeException("Router#putExtraData data is null")
        ManagerRegistry.DATA.putData(webUrl, data)
    }

    private fun openApp(ctx: Context, params: JSONObject) {
        openBrowser(ctx, params)
    }

    private fun openWeb(ctx: Context, params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openWeb url is null")
        val result = CubeWx.mWxRouter.openWeb(ctx, webUrl)
        if (!result.first) {
            throw RuntimeException("Router#openWeb Error ${result.second}")
        }
    }

    private fun openDialog(act: AppCompatActivity, params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openDialog url is null")
        val configJsonObj = params.getJSONObject(KEY_CONFIG)
        val config = if (configJsonObj != null) {
            configJsonObj.toObjEx(DialogConfig::class.java)
        } else {
            DialogConfig()
        }
        val result = CubeWx.mWxRouter.openDialog(act, webUrl, config)
        if (!result.first) {
            throw RuntimeException("Router#openDialog Error ${result.second}")
        }
    }

    private fun openBrowser(ctx: Context, params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openBrowser url is null")
        val result = CubeWx.mWxRouter.openBrowser(ctx, webUrl)
        if (!result.first) {
            throw RuntimeException("Router#openBrowser Error ${result.second}")
        }
    }

    private fun openUrl(ctx: Context, params: JSONObject) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openUrl url is null")
        val result = CubeWx.mWxRouter.openUrl(ctx, webUrl)
        if (!result.first) {
            throw RuntimeException("Router#openUrl Error ${result.second}")
        }
    }

}