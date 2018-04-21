package com.march.wxcube


/**
 * CreateAt : 2018/4/20
 * Describe :
 *
 * @author chendong
 */
data class WeexConfig(
        var jsLoadStrategy: Int = WeexJsLoader.JsLoadStrategy.DEFAULT,
        var jsCacheStrategy: Int = WeexJsLoader.JsCacheStrategy.PREPARE_ALL,
        var jsCacheMaxSize: Int = -1,
        var debug: Boolean = true
)