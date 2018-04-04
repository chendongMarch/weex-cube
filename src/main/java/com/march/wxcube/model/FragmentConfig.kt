package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable


/**
 * CreateAt : 2018/3/29
 * Describe :
 *
 * @author chendong
 */
class FragmentConfig() : Parcelable {

    var tag: String? = null // 显示的唯一标示

    var url: String? = null // 内容 url -> WeexPage

    var lazyLoad = false // 是不是懒加载，懒加载的话会在 showTab 调用时才开始架子啊

    var indexPage = false // 是不是默认加载的页面

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.tag)
        dest.writeString(this.url)
        dest.writeByte(if (lazyLoad) 1 else 0)
        dest.writeByte(if (indexPage) 1 else 0)
    }

    constructor(`in`: Parcel) : this() {
        this.tag = `in`.readString()
        this.url = `in`.readString()
        this.lazyLoad = `in`.readByte() == 1.toByte()
        this.indexPage = `in`.readByte() == 1.toByte()
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FragmentConfig> = object : Parcelable.Creator<FragmentConfig> {
            override fun createFromParcel(source: Parcel): FragmentConfig = FragmentConfig(source)
            override fun newArray(size: Int): Array<FragmentConfig?> = arrayOfNulls(size)
        }
    }
}
