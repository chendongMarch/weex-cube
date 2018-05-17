package com.march.wxcube

import android.app.Application
import com.march.wxcube.common.memory
import java.io.File


/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
class WeexConfig(val ctx: Application) {

    var debug: Boolean = true
    var jsLoadStrategy: Int = JsLoadStrategy.DEFAULT
    var jsCacheStrategy: Int = JsCacheStrategy.PREPARE_ALL
    var configUrl: String = ""
    var envs: Map<String, String>? = null
    var nowEnv: String = ""

    fun prepare(): WeexConfig {
        return this
    }
}