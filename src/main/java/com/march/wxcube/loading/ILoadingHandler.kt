package com.march.wxcube.loading

import android.view.ViewGroup

/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
interface ILoadingHandler {

    fun addLoadingView(container: ViewGroup?)

    fun finish(container: ViewGroup?)

}