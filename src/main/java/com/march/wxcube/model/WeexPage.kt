package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils


/**
 * CreateAt : 2018/3/26
 * Describe : 页面资源数据结构
 *
 * @author chendong
 */
class WeexPage() : Parcelable {

    var _comment: String? = null // 对该页面的注释，生产环境不返回

    var pageName: String? = null // 页面名称

    var jsVersion: String? = null // weex 构建版本

    var remoteJs: String? = null // 远程 js

    var appVersion: String? = null // app 最小支持版本

    var webUrl: String? = null  // 降级 web，也是页面的唯一标记

    var indexPage: Boolean = false // 该页面上是否被标记为首页

    /*********************⬆️以上是数据结构字段⬇️下面是临时生成字段************************/

    val key: String?
        get() = "$pageName($jsVersion)".replace(".", "")

    val localJs: String? // 本地 js 文件名
        get() = "$pageName-$jsVersion-js".replace(".", "-")

    val assetsJs: String? // assets js 文件名
        get() = "js/$pageName-$jsVersion-js".replace(".", "-")

    val isValid: Boolean //
        get() = (!TextUtils.isEmpty(pageName)
                && !TextUtils.isEmpty(jsVersion)
                && !TextUtils.isEmpty(webUrl)
                && !TextUtils.isEmpty(remoteJs))

    fun make(openUrl: String): WeexPage {
        val page = WeexPage()
        page.pageName = this.pageName
        page.remoteJs = this.remoteJs
        page.appVersion = this.appVersion
        page.jsVersion = this.jsVersion
        page.webUrl = openUrl
        return page
    }

    override fun toString(): String {
        return "WeexPage{" +
                "pageName='" + pageName + '\'' +
                ", remoteJs='" + remoteJs + '\'' +
                ", localJs='" + localJs + '\'' +
                ", assetsJs='" + assetsJs + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", jsVersion='" + jsVersion + '\'' +
                ", webUrl='" + webUrl + '\'' +
                '}'
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.pageName)
        dest.writeString(this.remoteJs)
        dest.writeString(this.localJs)
        dest.writeString(this.assetsJs)
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


    companion object {

        const val KEY_PAGE = "KEY_PAGE"

        @JvmField
        val CREATOR: Parcelable.Creator<WeexPage> = object : Parcelable.Creator<WeexPage> {
            override fun createFromParcel(source: Parcel): WeexPage = WeexPage(source)
            override fun newArray(size: Int): Array<WeexPage?> = arrayOfNulls(size)
        }
    }
}
