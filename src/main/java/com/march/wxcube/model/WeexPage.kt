package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils


/**
 * CreateAt : 2018/3/26
 * Describe :
 *
 * @author chendong
 */
class WeexPage() : Parcelable {

    var pageName: String? = null

    var remoteJs: String? = null // 远程 js

    var localJs: String? = null // 本地 js

    var assetsJs: String? = null // assets js

    var appVersion: String? = null // app 最小支持版本

    var jsVersion: String? = null // weex 构建版本

    var jsMd5: String? = null // js 的 md5 值，用来标记文件有没有修改

    var webUrl: String? = null  // 降级 web，也是唯一标记

    val isValid: Boolean
        get() = (!TextUtils.isEmpty(pageName)
                && !TextUtils.isEmpty(jsVersion)
                && !TextUtils.isEmpty(webUrl)
                && isAnyJsValid)

    private val isAnyJsValid: Boolean
        get() = (!TextUtils.isEmpty(remoteJs)
                || !TextUtils.isEmpty(assetsJs)
                || !TextUtils.isEmpty(localJs))

    fun make(openUrl: String): WeexPage {
        val page = WeexPage()
        page.pageName = this.pageName
        page.remoteJs = this.remoteJs
        page.localJs = this.localJs
        page.assetsJs = this.assetsJs
        page.appVersion = this.appVersion
        page.jsVersion = this.jsVersion
        page.jsMd5 = this.jsMd5
        page.webUrl = openUrl
        return page
    }

    override fun hashCode(): Int {
        return 43
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is WeexPage) {
            return false
        }
        return pageName == other.pageName && jsVersion == other.jsVersion
    }


    override fun toString(): String {
        return "WeexPage{" +
                "pageName='" + pageName + '\'' +
                ", remoteJs='" + remoteJs + '\'' +
                ", localJs='" + localJs + '\'' +
                ", assetsJs='" + assetsJs + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", jsVersion='" + jsVersion + '\'' +
                ", jsMd5='" + jsMd5 + '\'' +
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
        dest.writeString(this.jsMd5)
        dest.writeString(this.webUrl)
    }

    constructor(`in`: Parcel) : this() {
        this.pageName = `in`.readString()
        this.remoteJs = `in`.readString()
        this.localJs = `in`.readString()
        this.assetsJs = `in`.readString()
        this.appVersion = `in`.readString()
        this.jsVersion = `in`.readString()
        this.jsMd5 = `in`.readString()
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
