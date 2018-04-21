package com.march.wxcube.manager

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.march.common.utils.NetUtils
import com.march.wxcube.Weex
import com.march.wxcube.http.LogInterceptor
import com.march.wxcube.model.WeexPage
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.IWXHttpAdapter
import com.taobao.weex.common.WXRequest
import com.taobao.weex.common.WXResponse
import okhttp3.*
import okhttp3.internal.Util
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
class HttpManager : IManager {


    companion object {
        const val KEY_TAG = "http-tag"
        val instance: HttpManager by lazy { HttpManager() }
    }

    private val mOkHttpClient: OkHttpClient by lazy { buildOkHttpClient() }


    override fun onWxInstRelease(weexPage: WeexPage?, instance: WXSDKInstance?) {
        val tag = instance?.instanceId ?: return
        val filter: (Call?) -> Boolean = {
            it != null && !it.isCanceled && it.request().tag() == tag
        }
        mOkHttpClient.dispatcher().queuedCalls()
                .filter(filter)
                .forEach { it.cancel() }
        mOkHttpClient.dispatcher().runningCalls()
                .filter(filter)
                .forEach { it.cancel() }
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
        // 连接超时
        builder.connectTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 读超时
        builder.readTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 写超时
        builder.writeTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 失败后重试
        builder.retryOnConnectionFailure(true)

        // 进行日志打印，扩展自 HttpLoggingInterceptor
        builder.addInterceptor(LogInterceptor())

        // face book 调试框架
        builder.addNetworkInterceptor(StethoInterceptor())
        // token校验，返回 403 时
        // builder.authenticator(new TokenAuthenticator());
        Weex.getInst().mWeexInjector.onInitOkHttpClient(builder)
        return builder.build()
    }

    fun requestSync(wxRequest: WXRequest): WXResponse {
        val netResp = checkNetWork(null)
        if (netResp != null) {
            return netResp
        }
        var wxResp = WXResponse()
        val request = makeRequest(wxRequest)
        val failure: (e: Exception?) -> WXResponse = {
            val resp = WXResponse()
            resp.errorCode = "-2"
            resp.statusCode = "-2"
            resp.errorMsg = "请求失败 ${it?.message} "
            resp
        }
        try {
            val response = mOkHttpClient.newCall(request).execute()
            if (response == null) {
                wxResp = failure(IOException("wxResp is null"))
            } else if (response.isSuccessful) {
                wxResp.errorCode = "0"
                wxResp.statusCode = response.code().toString()
                wxResp.originalData = response.body()?.bytes()
            }
        } catch (e: Exception) {
            wxResp = failure(e)
        }
        return wxResp
    }

    fun request(wxRequest: WXRequest, listener: IWXHttpAdapter.OnHttpListener) {
        if (checkNetWork(listener) != null) {
            return
        }
        val wxResp = WXResponse()
        val request = makeRequest(wxRequest)
        listener.onHttpStart()
        mOkHttpClient.newCall(request)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call?, response: Response?) {
                        if (response == null) {
                            onFailure(call, IOException("wxResp is null"))
                        } else if (response.isSuccessful) {
                            wxResp.errorCode = "1"
                            wxResp.statusCode = response.code().toString()
                            wxResp.originalData = response.body()?.bytes()
                            listener.onHttpFinish(wxResp)
                        }
                    }

                    override fun onFailure(call: Call?, e: IOException?) {
                        wxResp.errorCode = "-2"
                        wxResp.statusCode = "-2"
                        wxResp.errorMsg = "请求失败 ${e?.message}"
                        listener.onHttpFinish(wxResp)
                    }
                })
    }


    private fun checkNetWork(listener: IWXHttpAdapter.OnHttpListener?): WXResponse? {
        if (!NetUtils.isNetworkConnected(Weex.getInst().getContext())) {
            val wxResp = WXResponse()
            wxResp.errorCode = "-1"
            wxResp.statusCode = "-1"
            wxResp.errorMsg = "网络未连接"
            listener?.onHttpFinish(wxResp)
            return wxResp
        }
        return null
    }

    private fun makeRequest(wxRequest: WXRequest): Request {
        val method = if (wxRequest.method == null) "get" else wxRequest.method
        val body = wxRequest.body
        val url = wxRequest.url
        val paramMap = wxRequest.paramMap
        var reqBuilder = Request.Builder().url(url)
        // header
        for (entry in paramMap) {
            if (entry.key == KEY_TAG) {
                reqBuilder.tag(entry.value)
            } else {
                reqBuilder.addHeader(entry.key, entry.value)
            }
        }
        // method
        val reqBodyCreator = {
            if (body.isNullOrEmpty()) {
                Util.EMPTY_REQUEST
            } else {
                RequestBody.create(MediaType.parse(body), body)
            }
        }
        reqBuilder = when (method) {
            "get" -> reqBuilder.get()
            "post" -> reqBuilder.post(reqBodyCreator())
            "put" -> reqBuilder.put(reqBodyCreator())
            "delete" -> reqBuilder.delete(reqBodyCreator())
            "patch" -> reqBuilder.patch(reqBodyCreator())
            "head" -> reqBuilder.head()
            else -> {
                reqBuilder
            }
        }
        return reqBuilder.build()
    }


    open class HttpListener : IWXHttpAdapter.OnHttpListener {
        override fun onHttpUploadProgress(uploadProgress: Int) {

        }

        override fun onHttpFinish(response: WXResponse?) {
        }

        override fun onHttpResponseProgress(loadedLength: Int) {
        }

        override fun onHeadersReceived(statusCode: Int, headers: MutableMap<String, MutableList<String>>?) {
        }

        override fun onHttpStart() {
        }

    }
}