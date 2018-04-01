package com.march.wxcube.utils

import com.bumptech.glide.util.Util

/**
 * CreateAt : 2018/4/1
 * Describe : bytes 池
 *
 * @author chendong
 */
class BytesPool private constructor() {

    private val tempQueue = Util.createQueue<ByteArray>(0)
    /**
     * Returns a byte array by retrieving one from the pool if the pool is non empty or otherwise by creating a new
     * byte array.
     */
    val bytes: ByteArray
        get() {
            var result: ByteArray? = null
            synchronized(tempQueue) {
                result = tempQueue.poll()
            }
            if (result == null) {
                result = ByteArray(TEMP_BYTES_SIZE)
            }
            return result as ByteArray

        }

    /**
     * Removes all byte arrays from the pool.
     */
    fun clear() {
        synchronized(tempQueue) {
            tempQueue.clear()
        }
    }

    /**
     * Adds the given byte array to the pool if it is the correct size and the pool is not full and returns true if
     * the byte array was added and false otherwise.
     *
     * @param bytes The bytes to try to add to the pool.
     */
    fun releaseBytes(bytes: ByteArray): Boolean {
        if (bytes.size != TEMP_BYTES_SIZE) {
            return false
        }

        var accepted = false
        synchronized(tempQueue) {
            if (tempQueue.size < MAX_BYTE_ARRAY_COUNT) {
                accepted = true
                tempQueue.offer(bytes)
            }
        }
        return accepted
    }

    companion object {
        // 64 KB.
        private val TEMP_BYTES_SIZE = 64 * 1024
        // 512 KB.
        private val MAX_SIZE = 2 * 1048 * 1024
        private val MAX_BYTE_ARRAY_COUNT = MAX_SIZE / TEMP_BYTES_SIZE
        private val BYTE_ARRAY_POOL = BytesPool()

        fun get(): BytesPool {
            return BYTE_ARRAY_POOL
        }
    }
}
