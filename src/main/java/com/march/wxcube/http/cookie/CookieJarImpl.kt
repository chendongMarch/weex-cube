package com.march.wxcube.http.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * CreateAt : 2018/6/23
 * Describe :
 *
 * @author chendong
 */
class CookieJarImpl(val cookieStore: CookieStore) : CookieJar {

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore.add(url, cookies)
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore.get(url)
    }
}