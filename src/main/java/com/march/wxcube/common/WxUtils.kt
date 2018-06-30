package com.march.wxcube.common

import android.net.Uri
import android.os.Environment
import com.march.common.utils.DimensUtils
import com.march.common.utils.FileUtils
import com.march.wxcube.CubeWx
import com.taobao.weex.WXSDKManager
import java.io.File
import java.net.URLDecoder

/**
 * CreateAt : 2018/6/20
 * Describe :
 *
 * @author chendong
 */
object WxUtils {

    fun getWxPxByRealPx(px: Int): Float {
        val ratio = 750f / DimensUtils.WIDTH
        return px * ratio
    }

    // 创建根缓存文件夹
    fun makeRootCacheDir(): File {
        val sdFile = Environment.getExternalStorageDirectory()
        var rootFile = CubeWx.mWeakCtx.get()?.cacheDir ?: sdFile
        if (CubeWx.mWeexConfig.debug) {
            rootFile = sdFile
        }
        val cacheFile = File(rootFile, WxConstants.CACHE_ROOT_DIR_NAME)
        cacheFile.mkdirs()
        return cacheFile
    }

    fun makeCacheDir(key: String): File {
        val destDir = File(CubeWx.mRootCacheDir, key)
        destDir.mkdirs()
        return destDir
    }

    fun clearDiskCache() {
        FileUtils.delete(CubeWx.mRootCacheDir)
    }

    fun rewriteUrl(url: String?, type: String): String {
        if (url.isNullOrBlank()) {
            return url ?: ""
        }
        val uri = Uri.parse(url)
        val rewrite = WXSDKManager.getInstance().uriAdapter.rewrite(null, type, uri)
        return URLDecoder.decode(rewrite.toString(),"utf-8")
    }
}