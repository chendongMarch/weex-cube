package com.march.wxcube.common

import com.march.common.disklru.DiskLruCache
import java.io.File

/**
 * CreateAt : 2018/4/25
 * Describe : 文件 lru 缓存
 *
 * @author chendong
 */
open class DiskLruCache(dir: File, maxSize: Long) {

    private val diskCache by lazy { DiskLruCache.open(dir, 1, 1, maxSize) }

    fun read(key: String): String {
        return try {
            diskCache.get(key)?.getString(0)?:""
        } catch (e: Exception) {
            log("磁盘读取 key = $key", e)
            ""
        }
    }

    fun write(key: String, value: String?) {
        if (value == null) {
            return
        }
        try {
            val edit = diskCache.edit(key)
            edit?.set(0, value)
            edit?.commit()
        } catch (e: Exception) {
            log("磁盘写入 key = $key", e)
        }
    }

    fun close() {
        if (!diskCache.isClosed) {
            diskCache.close()
        }
    }

}
