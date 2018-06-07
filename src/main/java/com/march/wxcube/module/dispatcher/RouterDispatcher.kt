package com.march.wxcube.module.dispatcher

import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.module.jsonObj2Obj
import com.march.wxcube.module.mWeexDelegate
import com.taobao.weex.bridge.JSCallback

/**
 * CreateAt : 2018/6/6
 * Describe : 路由模块分发
 *
 * @author chendong
 */
class RouterDispatcher : AbsDispatcher() {

    companion object {
        // method
        const val openUrl = "openUrl"
        const val openWeb = "openWeb"
        const val openDialog = "openDialog"
        const val openBrowser = "openBrowser"
        const val openApp = "openApp"
        const val closePage = "closePage"
        const val putExtraData = "putExtraData"
    }

    override fun getMethods(): List<String> {
        return listOf(
                openUrl,
                openWeb,
                openDialog,
                openBrowser,
                openApp,
                closePage,
                putExtraData
        )
    }

    override fun dispatch(method: String, params: JSONObject, callback: JSCallback) {
        val act = findAct()
        when (method) {
            openUrl      -> openUrl(act, params, callback)
            openWeb      -> openWeb(act, params, callback)
            openDialog   -> openDialog(act, params, callback)
            openBrowser  -> openBrowser(act, params, callback)
            openApp      -> openApp(act, params, callback)
            closePage    -> closePage(act, params, callback)
            putExtraData -> putExtraData(act, params, callback)
        }
    }

    private fun closePage(ctx: Context, params: JSONObject, callback: JSCallback) {
        val delegate = mModule.mWeexDelegate ?: throw RuntimeException("Router#closePage delegate is null")
        delegate.close()
    }

    private fun putExtraData(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#putExtraData url is null")
        val data = params[KEY_DATA] ?: throw RuntimeException("Router#putExtraData data is null")
        ManagerRegistry.DATA.putData(webUrl, data)
        mModule.postJsResult(callback, true to "Router#putExtraData finish")
    }

    private fun openApp(ctx: Context, params: JSONObject, callback: JSCallback) {
        openBrowser(ctx, params, callback)
    }

    private fun openWeb(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openWeb url is null")
        mModule.postJsResult(callback, Weex.getInst().mWeexRouter.openWeb(ctx, webUrl))
    }

    private fun openDialog(act: AppCompatActivity, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openDialog url is null")
        val configJsonObj = params.getJSONObject(KEY_CONFIG)
        val config = if (configJsonObj != null) {
            mModule.jsonObj2Obj(configJsonObj, DialogConfig::class.java)
        } else {
            DialogConfig()
        }
        mModule.postJsResult(callback, Weex.getInst().mWeexRouter.openDialog(act, webUrl, config))
    }

    private fun openBrowser(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openBrowser url is null")
        mModule.postJsResult(callback, Weex.getInst().mWeexRouter.openBrowser(ctx, webUrl))
    }

    private fun openUrl(ctx: Context, params: JSONObject, callback: JSCallback) {
        val webUrl = params.getString(KEY_URL) ?: throw RuntimeException("Router#openUrl url is null")
        mModule.postJsResult(callback, Weex.getInst().mWeexRouter.openUrl(ctx, webUrl))
    }

}