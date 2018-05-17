package com.march.wxcube

import android.app.Activity
import android.util.Log
import com.march.wxcube.loading.ILoadingHandler
import com.march.wxcube.loading.LoadingHandlerImpl
import com.taobao.weex.InitConfig
import okhttp3.OkHttpClient

/**
 * CreateAt : 2018/4/1
 * Describe :
 *
 * @author chendong
 */
interface WeexInjector {

    fun onErrorReport(throwable: Throwable?, errorMsg: String) {

    }

    fun onLog(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun onInitWeex(builder: InitConfig.Builder) {}

    fun onPageCreated(activity: Activity, type: Int) {

    }

    fun onInitOkHttpClient(builder: OkHttpClient.Builder) {}

    fun getLoadingHandler(): ILoadingHandler {
        return LoadingHandlerImpl()
    }

    companion object {
        val EMPTY = object : WeexInjector {

        }
    }

}