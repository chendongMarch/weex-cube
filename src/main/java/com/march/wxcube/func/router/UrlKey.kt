package com.march.wxcube.func.router

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

    var url: String? = ""
    var host: String? = ""
    var port: String? = ""
    var path: String? = ""

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is UrlKey) {
            return false
        }
        return if (port == null || other.port == null) {
            host == other.host && path == other.path
        } else {
            host == other.host && port == other.port && path == other.path
        }
    }

    override fun hashCode(): Int {
        return 43
    }

}
