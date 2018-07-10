package com.march.wxcube.http.cookie

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.CookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

/**
 * CreateAt : 2018/7/9
 * Describe :
 *
 * @author chendong
 */
class PersistentCookieJarImpl(cache: CookieCache, prefs: SharedPrefsCookiePersistor) : PersistentCookieJar(cache, prefs) {

    var cache: CookieCache = cache
    var prefs: SharedPrefsCookiePersistor = prefs

}