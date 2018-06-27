package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
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

    var webUrl: String? = null  // 降级 web，也是页面的唯一标记

    var remoteJs: String? = null // 远程 js 资源链接

    var md5: String? = null // 远端生成 md5 用来校验下载文件完整性

    var comment: String? = null // 对该页面的注释，生产环境不返回

    var indexPage: Boolean = false // 该页面上是否被标记为首页

    /*********************⬆️以上是数据结构字段⬇️下面是临时生成字段************************/

    val key: String?
        get() = "$pageName($jsVersion)".replace(".", "")

    val localJs: String? // 本地 js 文件名 home-page-weex-1-1-0-js
        get() = "$pageName-$jsVersion-js".replace(".", "-")

    val assetsJs: String? // assets js 文件名 js/home-page-weex-1-1-0-js
        get() = "$pageName-$jsVersion-js".replace(".", "-")

    val isValid: Boolean //
        get() = (!TextUtils.isEmpty(pageName)
                && !TextUtils.isEmpty(jsVersion)
                && !TextUtils.isEmpty(webUrl)
                && !TextUtils.isEmpty(remoteJs))

    fun make(openUrl: String): WxPage {
        val page = WxPage()
        page.pageName = this.pageName
        page.remoteJs = this.remoteJs
        page.appVersion = this.appVersion
        page.jsVersion = this.jsVersion
        page.webUrl = openUrl
        return page
    }


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.pageName)
        dest.writeString(this.remoteJs)
        dest.writeString(this.appVersion)
        dest.writeString(this.jsVersion)
        dest.writeString(this.webUrl)
    }

    constructor(`in`: Parcel) : this() {
        this.pageName = `in`.readString()
        this.remoteJs = `in`.readString()
        this.appVersion = `in`.readString()
        this.jsVersion = `in`.readString()
        this.webUrl = `in`.readString()
    }

    override fun describeContents() = 0

    override fun toString(): String {
        return "WeexPage(pageName=$pageName, " +
                "jsVersion=$jsVersion, " +
                "appVersion=$appVersion, " +
                "webUrl=$webUrl, " +
                "remoteJs=$remoteJs, " +
                "md5=$md5, " +
                "comment=$comment, " +
                "indexPage=$indexPage)"
    }


    fun toShowString(): String {
        return StringBuilder()
                .append("pageName=").append(pageName).newLine()
                .append("remoteJs=").append(remoteJs).newLine()
                .append("appVersion=").append(appVersion).newLine()
                .append("jsVersion=").append(jsVersion).newLine()
                .append("webUrl=").append(webUrl).newLine().toString()
    }


    companion object {

        const val KEY_PAGE = "KEY_PAGE"

        @JvmField
        val CREATOR: Parcelable.Creator<WxPage> = object : Parcelable.Creator<WxPage> {
            override fun createFromParcel(source: Parcel): WxPage = WxPage(source)
            override fun newArray(size: Int): Array<WxPage?> = arrayOfNulls(size)
        }
    }
}
