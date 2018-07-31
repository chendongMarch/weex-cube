package com.march.wxcube.wxadapter

import android.net.Uri
import com.march.wxcube.CubeWx
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


    /*
     *   absolute 带有显式 scheme  pattern
     *                            "<scheme>://<authority><absolute path>?<query>#<fragment>"
     *   relative 没有显示 scheme  pattern
     *                            "//<authority><absolute path>?<query>#<fragment>"
     *                            "<relative or absolute path>?<query>#<fragment>"
     *   opaque   透明            pattern
     *                            "<scheme>:<opaque part>#<fragment>" like mailto:nobody@google.com
     *   hierarchical   分层      relative 总是分层的，absolute 如果 scheme 部分以 / 开头则是分层的
     *
     */
    override fun rewrite(instance: WXSDKInstance?, type: String, uri: Uri): Uri {
        if (type == URIAdapter.IMAGE) {
            return uri
        }
        val builder = uri.buildUpon()
        if (uri.isRelative && !uri.isOpaque) {
            addHttpSchema(builder, uri)
            when (type) {
                URIAdapter.REQUEST -> addAuthority(CubeWx.mWxCfg.reqAuthority, builder, uri)
                URIAdapter.WEB     -> addAuthority(CubeWx.mWxCfg.webAuthority, builder, uri)
                URIAdapter.BUNDLE  -> {
                    addAuthority(CubeWx.mWxCfg.bundleAuthority, builder, uri)
                    addPrefixPath(CubeWx.mWxCfg.bundlePathPrefix, builder, uri)
                }
            }
            return builder.build()
        } else if(URIAdapter.IMAGE == type){
            replaceHttpSchema(builder, uri)
            return builder.build()
        }
        return uri
    }

    private fun replaceHttpSchema(builder: Uri.Builder, uri: Uri): Uri.Builder {
        if (uri.scheme != null && uri.scheme.startsWith("http")) {
            builder.scheme("http")
        }
        return builder
    }


    // 检测添加 schema
    private fun addHttpSchema(builder: Uri.Builder, base: Uri): Uri.Builder {
        if (base.scheme != null) {
            return builder
        }
        if (CubeWx.mWxCfg.https) {
            builder.scheme("https")
        } else {
            builder.scheme("http")
        }
        return builder
    }

    // 添加
    private fun addAuthority(authority: String, builder: Uri.Builder, base: Uri): Uri.Builder {
        if (base.authority != null) {
            return builder
        }
        builder.authority(authority)
        return builder
    }

    private fun addPrefixPath(prefixPath: String, builder: Uri.Builder, base: Uri): Uri.Builder {
        builder.path(prefixPath + base.path)
        return builder
    }
}
