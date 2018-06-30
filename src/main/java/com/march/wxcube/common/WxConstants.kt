package com.march.wxcube.common

/**
 * CreateAt : 2018/6/29
 * Describe :
 *
 * @author chendong
 */
object WxConstants {

    // 混存路径
    const val CACHE_ROOT_DIR_NAME = "CubeWxCache"

    // 页面类型
    const val PAGE_WEB = 1
    const val PAGE_WEEX = 2
    const val PAGE_INDEX = 3

    // 错误
    const val ERR_PAGE_NOT_VALID = "ERR_PAGE_NOT_VALID" // 页面数据错误
    const val ERR_JS_NOT_READY = "ERR_JS_NOT_READY" // js 失败
}