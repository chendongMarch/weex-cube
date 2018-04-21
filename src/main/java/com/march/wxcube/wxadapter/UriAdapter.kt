package com.march.wxcube.wxadapter

import android.net.Uri

import com.taobao.weex.WXSDKInstance
import com.taobao.weex.adapter.DefaultUriAdapter
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class UriAdapter : DefaultUriAdapter() {
    override fun rewrite(instance: WXSDKInstance, type: String, uri: Uri): Uri {
        return super.rewrite(instance, type, uri)
    }
}
