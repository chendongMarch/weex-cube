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
class PageBundle() : Parcelable {
    var pageName: String? = null

    var remoteJs: String? = null // 远程 js

    var localJs: String? = null // 本地 js

    var assetsJs: String? = null // assets js

    var appVersion: String? = null // app 最小支持版本

    var jsVersion: String? = null // weex 构建版本

    var jsMd5: String? = null // js 的 md5 值，用来标记文件有没有修改

    var webUrl: String? = null  // 降级 web，也是唯一标记

    var realUrl: String? = null // 开启页面的 url,带参数

    val isValid: Boolean
        get() = (!TextUtils.isEmpty(pageName)
                && !TextUtils.isEmpty(jsVersion)
                && !TextUtils.isEmpty(webUrl)
                && isAnyJsValid)

    private val isAnyJsValid: Boolean
        get() = (!TextUtils.isEmpty(remoteJs)
                || !TextUtils.isEmpty(assetsJs)
                || !TextUtils.isEmpty(localJs))

    override fun hashCode(): Int {
        return 43
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || other !is PageBundle) {
            return false
        }
        return pageName == other.pageName && jsVersion == other.jsVersion
    }


    override fun toString(): String {
        return "PageBundle{" +
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
        dest.writeString(this.realUrl)
    }

    constructor(`in`: Parcel):this() {
        this.pageName = `in`.readString()
        this.remoteJs = `in`.readString()
        this.localJs = `in`.readString()
        this.assetsJs = `in`.readString()
        this.appVersion = `in`.readString()
        this.jsVersion = `in`.readString()
        this.jsMd5 = `in`.readString()
        this.webUrl = `in`.readString()
        this.realUrl = `in`.readString()
    }

    override fun describeContents() = 0


    companion object {

        const val KEY_PAGE = "KEY_PAGE"

        @JvmField
        val CREATOR: Parcelable.Creator<PageBundle> = object : Parcelable.Creator<PageBundle> {
            override fun createFromParcel(source: Parcel): PageBundle = PageBundle(source)
            override fun newArray(size: Int): Array<PageBundle?> = arrayOfNulls(size)
        }
    }
}
