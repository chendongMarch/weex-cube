package com.march.wxcube.common

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build


/**
 * CreateAt : 2018/6/4
 * Describe :
 *
 * @author chendong
 */
object Permission {

    fun checkPermission(act: Activity, permissions: Array<String>): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        val needReqPermission = mutableListOf<String>()
        permissions.forEach {
            if (!isPermissionOk(act, it)) {
                needReqPermission.add(it)
            }
        }
        if (needReqPermission.isEmpty()) {
            return true
        }
        act.requestPermissions(needReqPermission.toTypedArray(), 100)
        return false
    }

    private fun isPermissionOk(context: Context, permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}