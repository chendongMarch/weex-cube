package com.march.wxcube.http

import com.march.wxcube.Weex
import com.march.wxcube.http.cookie.CookieJarImpl
import com.march.wxcube.http.cookie.CookieStoreImpl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * CreateAt : 2018/6/23
 * Describe :
 *
 * @author chendong
 */
internal object OkHttpMaker {

     fun buildOkHttpClient(): OkHttpClient {

        val builder = OkHttpClient.Builder()
        // 连接超时
        builder.connectTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 读超时
        builder.readTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 写超时
        builder.writeTimeout(5 * 1000, TimeUnit.MILLISECONDS)
        // 失败后重试
        builder.retryOnConnectionFailure(true)
        // builder.proxy(Proxy.NO_PROXY)
        Weex.getContext()?.let {
            builder.cookieJar(CookieJarImpl(CookieStoreImpl(it)))
        }
        // 进行日志打印，扩展自 HttpLoggingInterceptor
        builder.addInterceptor(LogInterceptor())
        // token校验，返回 403 时
        // builder.authenticator(new TokenAuthenticator());
        Weex.mWeexInjector.onInitOkHttpClient(builder)
        return builder.build()
    }

}