package com.march.wxcube.http

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.march.wxcube.CubeWx
import com.march.wxcube.http.cookie.PersistentCookieJarImpl
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
        CubeWx.mWeakCtx.get()?.let {
            //            builder.cookieJar(CookieJarImpl(CookieStoreImpl(it)))
            builder.cookieJar(PersistentCookieJarImpl(SetCookieCache(), SharedPrefsCookiePersistor(it)))
        }
        // 进行日志打印，扩展自 HttpLoggingInterceptor
        builder.addInterceptor(LogInterceptor())
        // token校验，返回 403 时
        // builder.authenticator(new TokenAuthenticator());
        CubeWx.mWxInitAdapter.onInitOkHttpClient(builder)
        return builder.build()
     }

}