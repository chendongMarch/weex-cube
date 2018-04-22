package com.march.wxcube

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import java.io.File


/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
class WeexConfig(val application: Application) {

    var debug: Boolean = true
    var jsLoadStrategy: Int = JsLoadStrategy.DEFAULT
    var jsCacheStrategy: Int = JsCacheStrategy.PREPARE_ALL
    var jsMemoryCacheMaxSize: Int? = null
    var jsFileCacheMaxSize: Long? = null
    var jsFileCacheDir: File? = null
    var jsFileCache: WeexJsLoader.IJsFileCache? = null
    var jsMemoryCache: WeexJsLoader.JsMemoryCache? = null
    var envs: Map<Int, String>? = null
    var nowEnv: Int = -1

}