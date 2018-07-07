package com.march.wxcube.wxadapter


import android.content.Context
import com.taobao.weex.appfram.storage.DefaultWXStorage
import com.taobao.weex.appfram.storage.IWXStorageAdapter
import com.taobao.weex.appfram.storage.StorageResultHandler

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class StorageAdapter(context: Context?) : DefaultWXStorage(context) {

    private val memoryCache by lazy { mutableMapOf<String, String?>() }

    override fun setItem(key: String?, value: String?, listener: IWXStorageAdapter.OnResultReceivedListener?) {
        super.setItem(key, value, listener)
        key?.let {
            memoryCache[it] = value
        }
    }

    override fun getItem(key: String?, listener: IWXStorageAdapter.OnResultReceivedListener?) {
        val nonNullKey: String = if (key == null) {
            listener?.onReceived(StorageResultHandler.getItemResult(null))
            return
        } else {
            key
        }
        val result = memoryCache[nonNullKey]
        if (result != null) {
            listener?.onReceived(StorageResultHandler.getItemResult(result))
            return
        } else {
            super.getItem(nonNullKey) { data ->
                listener?.onReceived(data)
                val saved = data["data"]
                if (saved != null && saved is String && saved != "undefined") {
                    memoryCache[nonNullKey] = saved
                }
            }
        }
    }

    override fun removeItem(key: String?, listener: IWXStorageAdapter.OnResultReceivedListener?) {
        super.removeItem(key, listener)
        key?.let {
            memoryCache.remove(it)
        }
    }
}
