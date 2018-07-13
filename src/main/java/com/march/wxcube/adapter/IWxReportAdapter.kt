package com.march.wxcube.adapter

import com.march.common.utils.LgUtils

/**
 * CreateAt : 2018/6/27
 * Describe : 上报
 *
 * @author chendong
 */

interface IWxReportAdapter {

    companion object {
        const val CODE_JS_EXCEPTION  = "JsException JS执行出错" // js 执行出错
        const val CODE_RENDER_ERROR  = "JsRenderError JS渲染出错" // js 渲染报错
        const val CODE_MD5_NOT_MATCH =  "Md5NotMatch md5校验出错" // md5 校验出错
        const val CODE_JS_LOAD_ERROR = "JsLoadError JS下载出错" // js 下载出错

    }


    /**
     * 异常发布
     */
    fun report(code: String, msg: String, throwable: Throwable? = null)


    /**
     * log 打印
     */
    fun log(tag: String, msg: String, throwable: Throwable? = null)

}

open class DefaultWxReportAdapter : IWxReportAdapter {

    override fun report(code: String, msg: String, throwable: Throwable?) {

    }

    override fun log(tag: String, msg: String, throwable: Throwable?) {
        LgUtils.e(tag, msg + "  " + throwable?.message)
        throwable?.let {
            LgUtils.e(tag, throwable)
        }
    }
}