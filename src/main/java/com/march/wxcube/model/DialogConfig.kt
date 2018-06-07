package com.march.wxcube.model

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity
import android.view.ViewGroup
import com.march.wxcube.R


/**
 * CreateAt : 2018/3/28
 * Describe : 弹出弹窗时的配置
 *
 * @author chendong
 */
class DialogConfig() : Parcelable {


    var tag: String? = null // 弹窗唯一标记

    var width = ViewGroup.LayoutParams.MATCH_PARENT // 弹窗宽度，以 750 为基准

    var height = ViewGroup.LayoutParams.WRAP_CONTENT // 弹窗高度，以 750 为基准

    var alpha = 1f // 弹窗透明度

    var dim = .6f // 背景阴影

    var gravity = "center" // 位置，center、top、right、left、bottom

    var anim = "normal" // 动画，btc、normal

    val gravityParse: Int
        get() {
            when (gravity) {
                "center" -> return Gravity.CENTER
                "bottom" -> return Gravity.BOTTOM
                "top" -> return Gravity.TOP
                "left" -> return Gravity.LEFT
                "right" -> return Gravity.RIGHT
            }
            return Gravity.CENTER
        }

    val animParse: Int
        get() {
            when (anim) {
                "normal" -> return 0
                "btc" -> return R.style.dialog_anim_bottom_center
            }
            return 0
        }

    fun getWidthParse(context: Context): Int {
        if (width < 0) {
            return width
        }
        val widthPixels = context.resources.displayMetrics.widthPixels
        val rate = widthPixels / 750f
        return (rate * width).toInt()
    }

    fun getHeightParse(context: Context): Int {
        if (height < 0) {
            return height
        }
        val widthPixels = context.resources.displayMetrics.widthPixels
        val rate = widthPixels / 750f
        return (rate * height).toInt()
    }



    override fun describeContents() = 0


    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.tag)
        dest.writeInt(this.width)
        dest.writeInt(this.height)
        dest.writeFloat(this.alpha)
        dest.writeFloat(this.dim)
        dest.writeString(this.gravity)
        dest.writeString(this.anim)
    }

    constructor (`in`: Parcel):this() {
        this.tag = `in`.readString()
        this.width = `in`.readInt()
        this.height = `in`.readInt()
        this.alpha = `in`.readFloat()
        this.dim = `in`.readFloat()
        this.gravity = `in`.readString()
        this.anim = `in`.readString()
    }

    companion object {

        const val KEY_DIALOG_CONFIG = "KEY_DIALOG_CONFIG"

        @JvmField
        val CREATOR: Parcelable.Creator<DialogConfig> = object : Parcelable.Creator<DialogConfig> {
            override fun createFromParcel(source: Parcel): DialogConfig = DialogConfig(source)
            override fun newArray(size: Int): Array<DialogConfig?> = arrayOfNulls(size)
        }
    }
}
