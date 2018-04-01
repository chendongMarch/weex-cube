package com.march.wxcube.model

import android.os.Parcel
import android.os.Parcelable
import android.R.attr.tag



/**
 * CreateAt : 2018/3/29
 * Describe :
 *
 * @author chendong
 */
class FragmentConfig() : Parcelable {

    var tag: String? = null

    var url: String? = null


    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.tag)
        dest.writeString(this.url)
    }

    constructor(`in`: Parcel): this() {
        this.tag = `in`.readString()
        this.url = `in`.readString()
    }
    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<FragmentConfig> = object : Parcelable.Creator<FragmentConfig> {
            override fun createFromParcel(source: Parcel): FragmentConfig = FragmentConfig(source)
            override fun newArray(size: Int): Array<FragmentConfig?> = arrayOfNulls(size)
        }
    }
}
