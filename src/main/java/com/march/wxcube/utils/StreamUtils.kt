package com.march.wxcube.utils

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection

/**
 * CreateAt : 2017/12/8
 * Describe : IO流
 *
 * @author chendong
 */
object StreamUtils {

    // 关闭流
    private fun closeStream(vararg closeables: Closeable?) {
        closeables
                .filterNotNull()
                .forEach {
                    try {
                        it.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
    }

    /**
     * 打开一个网络流
     *
     * @param conn 网络连接
     * @return 流
     * @throws IOException error
     */
    @Throws(IOException::class)
    fun openHttpStream(conn: HttpURLConnection): InputStream {
        conn.requestMethod = "GET"
        conn.readTimeout = 3000
        conn.connectTimeout = 3000
        conn.doOutput = true
        conn.doInput = true
        // 设置通用的请求属性
        conn.setRequestProperty("accept", "*/*")
        conn.setRequestProperty("connection", "Keep-Alive")
        // 发起连接
        conn.connect()
        return conn.inputStream
    }

    /**
     * 保存文件到
     *
     * @param file 文件
     * @param inputStream   流
     * @return
     */
    fun saveStreamToFile(file: File, inputStream: InputStream): File? {
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(FileOutputStream(file))
            val bs = BytesPool.get().bytes
            var len: Int
            while (true) {
                len = bis.read(bs)
                if(len == -1){
                    break
                }
                bos.write(bs, 0, len)
                bos.flush()
            }
            BytesPool.get().releaseBytes(bs)
        } catch (e: Exception) {
            return null
        } finally {
            closeStream(bis, bos)
        }
        return file
    }

    fun saveStreamToBytes(inputStream: InputStream): ByteArray? {
        var bis: BufferedInputStream? = null
        var bos: ByteArrayOutputStream? = null
        val result: ByteArray
        try {
            bis = BufferedInputStream(inputStream)
            bos = ByteArrayOutputStream()
            val bs = BytesPool.get().bytes
            var len: Int
            while (true) {
                len = bis.read(bs)
                if(len == -1){
                    break
                }
                bos.write(bs, 0, len)
                bos.flush()
            }
            result = bos.toByteArray()
            BytesPool.get().releaseBytes(bs)
        } catch (e: Exception) {
            return null
        } finally {
            closeStream(bis, bos)
        }
        return result
    }

    /**
     * 从流中读取为字符串
     *
     * @param inputStream 流
     * @return json
     */
    fun saveStreamToString(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        var br: BufferedReader? = null
        var json: String? = null
        try {
            br = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String
            while (true) {
                line = br.readLine()
                if(line.isEmpty()){
                    break
                }
                sb.append(line)
            }
            json = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            closeStream(br)
        }
        return json
    }
}
