package com.march.wxcube.manager

import android.content.Context
import com.march.common.disklru.DiskLruCache
import com.march.common.pool.ExecutorsPool
import com.march.common.utils.NetUtils
import com.march.common.utils.StreamUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.http.HttpListener
import com.march.wxcube.http.OkHttpMaker
import com.march.wxcube.model.WxPage
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.IWXHttpAdapter
import com.taobao.weex.common.WXRequest
import com.taobao.weex.common.WXResponse
import okhttp3.*
import okhttp3.internal.Util
import java.io.File
import java.io.IOException

/**
 * CreateAt : 2018/4/20
 * Describe : 网络请求管理
 *
 * @author chendong
 */
class RequestManager : IManager {

    companion object {
        const val KEY_TAG = "http-tag"
        const val ERROR_CODE_SUCCESS = "0"
        const val ERROR_CODE_FAILURE = "-1"
        const val STATUS_CODE_SUCCESS = "200"
        const val STATUS_CODE_FAILURE = "-1"
        val instance: RequestManager by lazy { RequestManager() }
    }

    private val mOkHttpClient by lazy { OkHttpMaker.buildOkHttpClient() }

    // 结束该页面的请求
    override fun onWxInstRelease(weexPage: WxPage?, instance: WXSDKInstance?) {
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

    /**
     * 发起同步网络请求
     */
    fun requestSync(wxRequest: WXRequest, originData: Boolean = true): WXResponse {
        val netResp = checkNetWork(null)
        if (netResp != null) {
            return netResp
        }
        var wxResp = WXResponse()
        val request = makeHttpRequest(wxRequest)
        val failure: (e: Exception?) -> WXResponse = {
            val resp = WXResponse()
            resp.errorCode = ERROR_CODE_FAILURE
            resp.statusCode = STATUS_CODE_FAILURE
            resp.errorMsg = "请求失败 ${it?.message} "
            resp
        }
        try {
            val response = mOkHttpClient.newCall(request).execute()
            if (response == null) {
                wxResp = failure(IOException("wxResp is null"))
            } else if (response.isSuccessful) {
                wxResp.errorCode = ERROR_CODE_SUCCESS
                wxResp.statusCode = response.code().toString()
                if (originData) {
                    wxResp.originalData = response.body()?.bytes()
                } else {
                    wxResp.data = response.body()?.string()
                }
            }
        } catch (e: Exception) {
            wxResp = failure(e)
        }
        return wxResp
    }


    /**
     * 发起异步网络请求
     */
    fun request(wxRequest: WXRequest, listener: IWXHttpAdapter.OnHttpListener, originData: Boolean = true) {
        if (checkNetWork(listener) != null) {
            return
        }
        val wxResp = WXResponse()
        val request = makeHttpRequest(wxRequest)
        listener.onHttpStart()
        mOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call?, response: Response?) {
                if (response == null) {
                    onFailure(call, IOException("wxResp is null"))
                } else if (response.isSuccessful) {
                    listener.onHeadersReceived(response.code(), response.headers().toMultimap())
                    wxResp.errorCode = ERROR_CODE_SUCCESS
                    wxResp.statusCode = response.code().toString()
                    if (originData) {
                        wxResp.originalData = response.body()?.bytes()
                    } else {
                        wxResp.data = response.body()?.string()
                    }
                    listener.onHttpFinish(wxResp)
                }
            }

            override fun onFailure(call: Call?, e: IOException?) {
                wxResp.errorCode = ERROR_CODE_FAILURE
                wxResp.statusCode = STATUS_CODE_FAILURE
                wxResp.errorMsg = "请求失败 ${e?.message}"
                listener.onHttpFinish(wxResp)
            }
        })
    }

    // 检测网络状态，停止请求
    private fun checkNetWork(listener: IWXHttpAdapter.OnHttpListener?): WXResponse? {
        if (!NetUtils.isNetworkConnected(CubeWx.mWeakCtx.get())) {
            val wxResp = WXResponse()
            wxResp.errorCode = ERROR_CODE_FAILURE
            wxResp.statusCode = STATUS_CODE_FAILURE
            wxResp.errorMsg = "网络未连接"
            listener?.onHttpFinish(wxResp)
            return wxResp
        }
        return null
    }

    // 通过 weex 请求构建 http 请求
    private fun makeHttpRequest(wxRequest: WXRequest): Request {
        val method = if (wxRequest.method == null) "get" else wxRequest.method
        val url = ManagerRegistry.HOST.makeRequestUrl(wxRequest.url)
        val body = wxRequest.body
        val paramMap = wxRequest.paramMap
        var reqBuilder = Request.Builder().url(url)
        // header
        paramMap?.forEach { entry ->
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

    // 创建一个请求
    fun makeWxRequest(method: String = "get",
                      body: String = "",
                      paramMap: Map<String, String>? = null,
                      url: String?,
                      from: String = "weex-cube"): WXRequest {
        val wxRequest = WXRequest()
        wxRequest.method = method
        wxRequest.url = url
        wxRequest.body = body
        val map = paramMap?.toMutableMap()
        map?.set("from", from)
        wxRequest.paramMap = map
        return wxRequest
    }


    // 资源请求


    /**
     * assets 资源请求
     */
    fun requestAssets(context: Context, url: String, listener: HttpListener) {
        val wxResponse = WXResponse()
        ExecutorsPool.getInst().execute({
            try {
                wxResponse.data = StreamUtils.saveStreamToString(context.assets.open(url))
                wxResponse.errorCode = RequestManager.ERROR_CODE_SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                wxResponse.errorCode = RequestManager.ERROR_CODE_FAILURE
                wxResponse.data = ""
            }
            listener.onHttpFinish(wxResponse)
        })
    }

    /**
     * 文件资源请求
     */
    fun requestFile(url: String, listener: HttpListener) {
        listener.onHttpStart()
        val wxResponse = WXResponse()
        ExecutorsPool.getInst().execute({
            try {
                wxResponse.data = StreamUtils.saveStreamToString(File(url).inputStream())
                wxResponse.errorCode = RequestManager.ERROR_CODE_SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                wxResponse.errorCode = RequestManager.ERROR_CODE_FAILURE
                wxResponse.data = ""
            }
            listener.onHttpFinish(wxResponse)
        })
    }

    /**
     * 文件资源请求，基于 disklru
     */
    fun requestDiskCache(key: String, diskLruCache: DiskLruCache, listener: HttpListener) {
        listener.onHttpStart()
        val wxResponse = WXResponse()
        ExecutorsPool.getInst().execute({
            try {
                wxResponse.data = diskLruCache.get(key).getString(0)
                wxResponse.errorCode = RequestManager.ERROR_CODE_SUCCESS
            } catch (e: Exception) {
                e.printStackTrace()
                wxResponse.errorCode = RequestManager.ERROR_CODE_FAILURE
                wxResponse.data = ""
            }
            listener.onHttpFinish(wxResponse)
        })
    }
}