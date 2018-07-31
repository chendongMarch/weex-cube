package com.march.wxcube.common

import android.app.Activity
import android.net.Uri
import android.os.Build
import com.march.common.utils.DimensUtils
import com.march.common.utils.FileUtils
import com.march.wxcube.CubeWx
import com.taobao.weex.WXSDKManager
import com.taobao.weex.adapter.URIAdapter
import java.io.File
import java.lang.Exception
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * CreateAt : 2018/6/20
 * Describe :
 *
 * @author chendong
 */
object WxUtils {

    // 根据真正的px转换为750标准下的px
    fun getWxPxByRealPx(px: Int): Float {
        val ratio = 750f / DimensUtils.WIDTH
        return px * ratio
    }

    // 创建根缓存文件夹
    fun makeRootCacheDir(): File {
        val rootFile = CubeWx.mWeakCtx.get()?.cacheDir
        val cacheFile = File(rootFile, "${CubeWx.mWxInitAdapter.getAppKey()}-${WxConstants.CACHE_ROOT_DIR_NAME}")
        cacheFile.mkdirs()
        return cacheFile
    }

    // 创建一个单独的缓存文件夹
    fun makeCacheDir(key: String): File {
        val destDir = File(CubeWx.mRootCacheDir, key)
        destDir.mkdirs()
        return destDir
    }

    // 清空缓存文件
    fun clearDiskCache() {
        FileUtils.delete(CubeWx.mRootCacheDir)
    }



    // 重写 url
    fun rewriteUrl(url: String?, type: String): String {
        if (url.isNullOrBlank()) {
            return url ?: ""
        }
        val uri = Uri.parse(url)
        val rewrite = WXSDKManager.getInstance().uriAdapter.rewrite(null, type, uri)
        if(type == URIAdapter.REQUEST) {
            return rewrite.toString().replace("%3A",":")
        } else {
            return Uri.decode(rewrite.toString())
        }
    }

    // 创建一个 disk cache
    fun makeDiskCache(dirName: String, size: Long): DiskLruCache {
        return DiskLruCache(WxUtils.makeCacheDir(dirName), size)
    }

    // 结束界面
    fun finishActivity(act: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (!act.isFinishing && !act.isDestroyed) {
                act.finish()
                act.overridePendingTransition(0, 0)
            }
        } else {
            if (!act.isFinishing) {
                act.finish()
                act.overridePendingTransition(0, 0)
            }
        }
    }

    // 计算 md5
    fun md5(template: String): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val md5Data = BigInteger(1, md.digest(template.toByteArray(Charset.forName("utf-8"))))
            String.format("%032X", md5Data)
        } catch (e: Exception) {
            "${e.message}"
        }
    }
}