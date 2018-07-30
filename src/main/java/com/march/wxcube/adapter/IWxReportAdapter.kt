package com.march.wxcube.adapter

import android.content.Context
import com.march.common.utils.LgUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.tag
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

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

    /**
     * toast
     */

    fun toast(ctx: Context, msg: String, short: Boolean = true)

}

open class DefaultWxReportAdapter : IWxReportAdapter {
    override fun toast(ctx: Context, msg: String, short: Boolean) {
        if (short) ctx.toast(msg) else ctx.longToast(msg)
        CubeWx.mWxReportAdapter.log("toastLog", msg)
    }

    override fun report(code: String, msg: String, throwable: Throwable?) {

    }

    override fun log(tag: String, msg: String, throwable: Throwable?) {
        if (!CubeWx.mWxCfg.logEnable) {
            return
        }
        LgUtils.e(tag, msg)
        throwable?.let {
            LgUtils.e(tag, throwable)
        }
    }
}