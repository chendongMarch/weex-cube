package com.march.wxcube.router

import android.net.Uri

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
internal class UrlKey {

    companion object {
        internal fun fromUrl(url: String): UrlKey {
            val urlKey = UrlKey()
            val uri = Uri.parse(url)
            urlKey.url = url
            urlKey.host = uri.host
            urlKey.port = uri.port.toString()
            urlKey.path = uri.path
            return urlKey
        }
    }

    private var url: String = ""
    private var host: String = ""
    private var port: String = ""
    private var path: String = ""

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is UrlKey) {
            return false
        }
        return host == other.host && port == other.port && path == other.path
    }

    override fun hashCode(): Int {
        return 43
    }

}
