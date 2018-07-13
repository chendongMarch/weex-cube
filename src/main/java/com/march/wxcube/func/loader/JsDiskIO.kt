package com.march.wxcube.func.loader

import com.march.common.utils.StreamUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest

/**
 * CreateAt : 2018/7/13
 * Describe :
 *
 * @author chendong
 */
interface JsDiskIO {
    fun write(key: String, stream: InputStream): File
    fun read(key: String): InputStream
    fun readAsString(key: String): String
    fun md5(key: String): String
}

class JsDiskIOImpl(private val rootDir: File) : JsDiskIO {

    override fun md5(key: String): String {
        val file = makeFile(key)
        return md5(file)?:""
    }

    // 写入文件
    override fun write(key: String, stream: InputStream): File {
        val file = makeFile(key)
        StreamUtils.saveStreamToFile(file, stream)
        return file
    }

    // 读取文件
    override fun read(key: String): InputStream {
        val file = makeFile(key)
        return FileInputStream(file)
    }

    // 读取文件为字符串
    override fun readAsString(key: String): String {
        val inputStream = read(key)
        return StreamUtils.saveStreamToString(inputStream)
    }


    private fun makeFile(fileName: String): File {
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }
        val file = File(rootDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
    }

    private fun md5(file: File): String? {
        if (!file.isFile) {
            return null
        }
        var digest: MessageDigest?
        var inputStream: FileInputStream?
        val buffer = ByteArray(1024)
        var len: Int
        try {
            digest = MessageDigest.getInstance("MD5")
            inputStream = FileInputStream(file)
            len = inputStream.read(buffer, 0, 1024)
            while (len != -1) {
                digest!!.update(buffer, 0, len)
                len = inputStream.read(buffer, 0, 1024)
            }
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        val bigInt = BigInteger(1, digest!!.digest())
        return String.format("%032x", bigInt)
    }

}