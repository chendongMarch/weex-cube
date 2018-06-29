package com.march.wxcube.http.cookie

import okhttp3.Cookie
import okhttp3.HttpUrl

/**
 * CreateAt : 2018/6/23
 * Describe : Cookie 接口类 base OkHttp
 *
 * @author chendong
 */
interface CookieStore {

    fun add(uri: HttpUrl, cookies: List<Cookie>)

    fun get(uri: HttpUrl): List<Cookie>

    fun getCookies(): List<Cookie>

    fun remove(uri: HttpUrl, cookie: Cookie): Boolean

    fun removeAll(): Boolean

}
