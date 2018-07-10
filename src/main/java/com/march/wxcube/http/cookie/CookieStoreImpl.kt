package com.march.wxcube.http.cookie

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import com.march.common.pool.ExecutorsPool
import com.march.common.utils.LgUtils
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.experimental.and


/**
 * CreateAt : 2018/6/23
 * Describe : 与 OKHttp 对接，持久化存储
 *
 * @author chendong
 */
class CookieStoreImpl(context: Context) : CookieStore {

    companion object {
        private const val LOG_TAG = "PersistentCookieStore"
        private const val COOKIE_PREFS = "CookiePrefsFile"
        private const val COOKIE_NAME_PREFIX = "cookie_"
    }

    private val mCookies by lazy { mutableMapOf<String, ConcurrentHashMap<String, Cookie>>() }
    private val mCookiePrefs: SharedPreferences

    init {
        mCookiePrefs = context.getSharedPreferences(COOKIE_PREFS, Context.MODE_PRIVATE)
        ExecutorsPool.getInst().execute { backUpCookies() }
    }

    // 从存储获取 cookie, 耗时
    fun backUpCookies() {
        mCookies.clear()
        val prefsMap = mCookiePrefs.all
        prefsMap.forEach { key, value ->
            if (value != null && !(value as String).startsWith(COOKIE_NAME_PREFIX)) {
                val cookieNames = TextUtils.split(value, ",")
                for (name in cookieNames) {
                    val encodedCookie = mCookiePrefs.getString(COOKIE_NAME_PREFIX + name, null)
                    if (encodedCookie != null) {
                        val decodedCookie = decodeCookie(encodedCookie)
                        if (decodedCookie != null) {
                            if (!mCookies.containsKey(key))
                                mCookies[key] = ConcurrentHashMap()
                            mCookies[key]?.set(name, decodedCookie)
                        }
                    }
                }
            }
        }
    }

    // 存储 cookie 到磁盘
    private fun saveCookieToDisk(uri: HttpUrl, cookie: Cookie) {
        try {
            val name = getCookieToken(cookie)
            val prefsWriter = mCookiePrefs.edit()
            prefsWriter.putString(uri.host(), TextUtils.join(",", mCookies[uri.host()]?.keys))
            prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(SerializableHttpCookie(cookie)))
            prefsWriter.apply()
        }catch (e:Exception){

        }
    }


    private fun add(uri: HttpUrl, cookie: Cookie) {
        val name = getCookieToken(cookie)
        if (cookie.persistent()) {
            if (!mCookies.containsKey(uri.host())) {
                mCookies[uri.host()] = ConcurrentHashMap()
            }
            mCookies[uri.host()]?.set(name, cookie)
        } else {
            if (mCookies.containsKey(uri.host())) {
                mCookies[uri.host()]?.remove(name)
            } else {
                return
            }
        }

        ExecutorsPool.getInst().execute { saveCookieToDisk(uri, cookie) }
    }

    private fun getCookieToken(cookie: Cookie): String {
        return cookie.name() + cookie.domain()
    }

    override fun add(uri: HttpUrl, cookies: List<Cookie>) {
        for (cookie in cookies) {
            add(uri, cookie)
        }
    }

    override operator fun get(uri: HttpUrl): List<Cookie> {
        val ret = ArrayList<Cookie>()
        if (mCookies.containsKey(uri.host())) {
            val cookies = this.mCookies[uri.host()]?.values
            if (cookies != null) {
                for (cookie in cookies) {
                    if (isCookieExpired(cookie)) {
                        remove(uri, cookie)
                    } else {
                        ret.add(cookie)
                    }
                }
            }
        }

        return ret
    }

    private fun isCookieExpired(cookie: Cookie): Boolean {
        return cookie.expiresAt() < System.currentTimeMillis()
    }

    override fun removeAll(): Boolean {
        val prefsWriter = mCookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
        mCookies.clear()
        return true
    }

    override fun remove(uri: HttpUrl, cookie: Cookie): Boolean {
        val name = getCookieToken(cookie)
        if (mCookies.containsKey(uri.host()) && mCookies[uri.host()]?.containsKey(name) == true) {
            mCookies[uri.host()]?.remove(name)
            val prefsWriter = mCookiePrefs.edit()
            if (mCookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
                prefsWriter.remove(COOKIE_NAME_PREFIX + name)
            }
            prefsWriter.putString(uri.host(), TextUtils.join(",", mCookies[uri.host()]?.keys))
            prefsWriter.apply()
            return true
        }
        return false
    }

    override fun getCookies(): List<Cookie> {
        val ret = ArrayList<Cookie>()
        for (key in mCookies.keys)
            mCookies[key]?.values?.let { ret.addAll(it) }
        return ret
    }


    private fun encodeCookie(cookie: SerializableHttpCookie?): String? {
        if (cookie == null)
            return null
        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            LgUtils.e(e)
            return null
        }

        return byteArrayToHexString(os.toByteArray())
    }

    private fun decodeCookie(cookieString: String): Cookie? {
        try {
            val bytes = hexStringToByteArray(cookieString)
            val byteArrayInputStream = ByteArrayInputStream(bytes)
            var cookie: Cookie? = null
            try {
                val objectInputStream = ObjectInputStream(byteArrayInputStream)
                cookie = (objectInputStream.readObject() as SerializableHttpCookie).getCookie()
            } catch (e: IOException) {
                Log.d(LOG_TAG, "IOException in decodeCookie", e)
            } catch (e: ClassNotFoundException) {
                Log.d(LOG_TAG, "ClassNotFoundException in decodeCookie", e)
            }
            return cookie
        }catch (e:Exception) {
            return null
        }
    }

    /**
     * Using some super basic byte array &lt;-&gt; hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
     */
    private fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v = element and 0xff.toByte()
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v.toInt()))
        }
        return sb.toString().toUpperCase(Locale.US)
    }

    /**
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}