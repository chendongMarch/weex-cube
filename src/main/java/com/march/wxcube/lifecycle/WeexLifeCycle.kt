package com.march.wxcube.lifecycle

import android.content.Intent
import android.view.View

/**
 * CreateAt : 2018/3/27
 * Describe :
 *
 * @author chendong
 */
interface WeexLifeCycle {

    fun onCreate() {}

    fun onViewCreated(view: View?) {}

    fun onStart() {}

    fun onResume() {}

    fun onPause() {}

    fun onStop() {}

    fun onDestroy() {}

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {}

    fun onPermissionResult(requestCode: Int, resultCode: Int, data: Intent) {}

}
