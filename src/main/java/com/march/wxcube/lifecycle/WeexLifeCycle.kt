package com.march.wxcube.lifecycle

import android.content.Intent

/**
 * CreateAt : 2018/3/27
 * Describe :
 *
 * @author chendong
 */
interface WeexLifeCycle {

    fun onCreate()

    fun onStart()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onDestroy()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent)

    fun close()

}
