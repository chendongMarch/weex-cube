package com.march.wxcube.http

import com.march.wxcube.CubeWx
import com.march.wxcube.http.cookie.CookieJarImpl
import com.march.wxcube.http.cookie.CookieStoreImpl
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
            builder.cookieJar(CookieJarImpl(CookieStoreImpl(it)))
        }
         builder.hostnameVerifier({ _, _ ->
             true
         })
         builder.sslSocketFactory(createSSLSocketFactory())
        // 进行日志打印，扩展自 HttpLoggingInterceptor
//        builder.addInterceptor(LogInterceptor())
        // token校验，返回 403 时
        // builder.authenticator(new TokenAuthenticator());
        CubeWx.mWxInitAdapter.onInitOkHttpClient(builder)
        return builder.build()
    }

    private fun createSSLSocketFactory(): SSLSocketFactory? {
        var ssfFactory: SSLSocketFactory? = null

        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, arrayOf<TrustManager>(TrustAllCerts()), SecureRandom())

            ssfFactory = sc.getSocketFactory()
        } catch (e: Exception) {
        }

        return ssfFactory
    }

    private class TrustAllCerts : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }

        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String) {

        }
    }

}