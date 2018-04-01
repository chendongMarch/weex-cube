package com.march.wxcube.extension;

import android.text.TextUtils
import java.io.File

/**
 * CreateAt : 2018/4/1
 * Describe :
 *
 * @author chendong
 */
fun <E> Collection<E>?.isEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun <E> Array<E>?.isEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun ByteArray?.isEmpty(): Boolean {
    return this == null || this.isEmpty()
}

fun CharSequence.isEmpty(): Boolean {
    return TextUtils.isEmpty(this)
}

fun File?.isEmpty(): Boolean {
    return !(this != null && this.exists() && this.length() > 0)
}

fun Int?.isEmpty(): Boolean {
    return this == null || this == 0
}

fun Long?.isEmpty(): Boolean {
    return this == null || this == 0L
}

fun Map<*, *>?.isEmpty(): Boolean {
    return this == null || this.keys.isEmpty();
}
