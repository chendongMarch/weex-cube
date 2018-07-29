package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.newLine


/**
 * CreateAt : 2018/3/26
 * Describe : 页面资源数据结构
 *
 * @author chendong
 */
class WxPage() : Parcelable {

    var pageName: String? = null // 页面名称

    var appVersion: String? = null // app 最小支持版本

    var jsVersion: String? = null // weex 构建版本出来的 js 版本 pageName + jsVersion 唯一标记一个资源

    var h5Url: String? = null  // 降级 web，也是页面的唯一标记

    var remoteJs: String? = null // 远程 js 资源链接

    var md5: String = "" // 远端生成 md5 用来校验下载文件完整性

    var comment: String? = null // 对该页面的注释，生产环境不返回

    var indexPage: Boolean = false // 该页面上是否被标记为首页

    /*********************⬆️以上是数据结构字段⬇️下面是临时生成字段************************/

    val key: String // page-name-weex-2-1-3
        get() = "$pageName-$jsVersion".replace(".", "-")

    val localJs: String // 本地 js 文件名  page-name-weex-2-1-3.0
        get() = key

    val assetsJs: String // assets js 文件名  page-name-weex-2-1-3.js
        get() = "$key.js"

    val isValid: Boolean //
        get() = (!TextUtils.isEmpty(pageName)
                && !TextUtils.isEmpty(jsVersion)
                && !TextUtils.isEmpty(h5Url)
                && !TextUtils.isEmpty(remoteJs))




    fun make(openUrl: String): WxPage {
        val page = WxPage()
        page.pageName = this.pageName
        page.remoteJs = this.remoteJs
        page.appVersion = this.appVersion
        page.jsVersion = this.jsVersion
        page.h5Url = openUrl
        page.indexPage = this.indexPage
        page.md5 = this.md5
        return page
    }


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.pageName)
        dest.writeString(this.remoteJs)
        dest.writeString(this.appVersion)
        dest.writeString(this.jsVersion)
        dest.writeString(this.h5Url)
        dest.writeString(this.md5)
        dest.writeByte(if (this.indexPage) 1 else 0)
    }

    constructor(`in`: Parcel) : this() {
        this.pageName = `in`.readString()
        this.remoteJs = `in`.readString()
        this.appVersion = `in`.readString()
        this.jsVersion = `in`.readString()
        this.h5Url = `in`.readString()
        this.md5 = `in`.readString()
        this.indexPage = `in`.readByte() == 1.toByte()
    }

    override fun describeContents() = 0


    fun toShowString(): String {
        return StringBuilder()
                .append("pageName=").append(pageName).newLine()
                .append("remoteJs=").append(remoteJs).newLine()
                .append("appVersion=").append(appVersion).newLine()
                .append("jsVersion=").append(jsVersion).newLine()
                .append("h5Url=").append(h5Url).newLine().toString()
    }

    fun toSimpleString(): String {
        return "$pageName $appVersion $jsVersion"
    }

    override fun toString(): String {
        return "WxPage(pageName=$pageName, " +
                "appVersion=$appVersion, " +
                "jsVersion=$jsVersion, " +
                "h5Url=$h5Url, " +
                "remoteJs=$remoteJs, " +
                "md5=$md5, " +
                "comment=$comment, " +
                "indexPage=$indexPage)"
    }

    companion object {

        const val KEY_PAGE = "KEY_PAGE"

        fun errorPage(): WxPage? {
            return CubeWx.mWxRouter.findPage(CubeWx.mWxPageAdapter.getNotFontPageUrl())
        }

        @JvmField
        val CREATOR: Parcelable.Creator<WxPage> = object : Parcelable.Creator<WxPage> {
            override fun createFromParcel(source: Parcel): WxPage = WxPage(source)
            override fun newArray(size: Int): Array<WxPage?> = arrayOfNulls(size)
        }
    }
}
