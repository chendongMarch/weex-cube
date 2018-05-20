package com.march.wxcube

import android.app.Application


/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
class WeexConfig(val ctx: Application) {

    var https: Boolean = false
    var debug: Boolean = true
    var jsLoadStrategy: Int = JsLoadStrategy.DEFAULT
    var jsCacheStrategy: Int = JsCacheStrategy.CACHE_MEMORY_DISK_BOTH
    var jsPrepareStrategy: Int = JsPrepareStrategy.PREPARE_ALL
    var configUrl: String = ""
    var envs: Map<String, String>? = null
    var nowEnv: String = ""

    fun prepare(): WeexConfig {
        return this
    }
}