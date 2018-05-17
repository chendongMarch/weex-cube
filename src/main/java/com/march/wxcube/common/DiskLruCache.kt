package com.march.wxcube.common

import com.march.common.disklru.DiskLruCache
import java.io.Closeable
import java.io.File

/**
 * CreateAt : 2018/4/25
 * Describe : 文件 lru 缓存
 *
 * @author chendong
 */
open class DiskLruCache(dir: File, maxSize: Long) {

    private val diskCache by lazy { DiskLruCache.open(dir, 1, 1, maxSize) }

    fun read(key: String): String? {
        return diskCache.get(key)?.getString(0)
    }

    fun write(key: String, value: String) {
        val edit = diskCache.edit(key)
        edit?.set(0, value)
        edit?.commit()
    }

    fun close() {
        if (!diskCache.isClosed) {
            diskCache.close()
        }
    }

}
