package com.march.wxcube.debug

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.march.common.utils.JsonUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.R
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.click
import com.march.wxcube.common.newLine
import kotlinx.android.synthetic.main.debug_dialog.*

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
class WxDebugDialog(context: Context, private val mWeexPageDebugger: WxPageDebugger)
    : AppCompatDialog(context, R.style.dialog_theme) {

    init {
        setContentView(R.layout.debug_dialog)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setDialogAttributes(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f, .6f, Gravity.BOTTOM)
            window.setWindowAnimations(R.style.dialog_anim_bottom_center)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        initTopDebug()
        initMultiPageDebug()
        initGlobalDebug()
        initCurPageDebug()
    }

    // 顶部工具
    private fun initTopDebug() {
        debugInfoBtn.click {
            ToastUtils.showLong("""
                    1. 长按可以触发强制刷新
                    2. 当在实时刷新时，图标会保持旋转作为提示
                """.trimIndent())
        }
        // 关闭
        debugCloseBtn.click { dismiss() }
    }

    // 页面调试
    private fun initCurPageDebug() {
        pageHideMsgBtn.click {
            if (pageMsgTv?.visibility == View.GONE) {
                pageMsgTv?.visibility = View.VISIBLE
            } else {
                pageMsgTv?.visibility = View.GONE
            }
        }
        updateMsg()
        val debugConfig = mWeexPageDebugger.mWxPageDebugCfg
        pageRefreshJsSwitch?.isChecked = debugConfig.isRefreshing
        pageRefreshJsSwitch?.setOnCheckedChangeListener { _, isChecked ->
            debugConfig.isRefreshing = isChecked
            if (debugConfig.isRefreshing) {
                mWeexPageDebugger.stopRefresh()
                mWeexPageDebugger.startRefresh(false)
                ToastUtils.show("开始调试远程js")
            } else {
                mWeexPageDebugger.stopRefresh()
                ToastUtils.show("停止调试远程js")
            }
        }
    }

    // 全局调试
    private fun initGlobalDebug() {
        // 自动刷新ip js
        globalForceNetJsSwitch.isChecked = WxGlobalDebugger.mWxDebugCfg.isForceNetJs
        globalForceNetJsSwitch.setOnCheckedChangeListener { _, isChecked ->
            WxGlobalDebugger.mWxDebugCfg.apply {
                isForceNetJs = isChecked
                flush()
            }
        }
        globalAutoRefreshJsSwitch.isChecked = WxGlobalDebugger.mWxDebugCfg.isAutoRefreshLocalIp
        globalAutoRefreshJsSwitch.setOnCheckedChangeListener({ _, isChecked ->
            WxGlobalDebugger.mWxDebugCfg.apply {
                isAutoRefreshLocalIp = isChecked
                flush()
            }
        })
        // 请求线上配置
        globalUpdateOnlineCfgBtn?.click { CubeWx.mWxUpdater.update(context) }
        // 请求调试配置
        globalUpdateDebugCfgBtn?.click { WxGlobalDebugger.updateFromNet() }
        // 清理缓存的js
        globalClearCacheBtn?.click { CubeWx.mWxJsLoader.clearCache() }
        // 清理磁盘js
        globalClearDiskBtn?.click { WxUtils.clearDiskCache() }
        globalJumpWeexBtn?.click {
            val text = globalJumpEt?.text.toString()
            if (!text.isBlank()) {
                CubeWx.mWxRouter.openUrl(context, text)
            }
        }
        globalJumoWebBtn?.click {
            val text = globalJumpEt?.text.toString()
            if (!text.isBlank()) {
                CubeWx.mWxRouter.openWeb(context, text)
            }
        }
    }

    // 多页面调试
    private fun initMultiPageDebug() {
        multiPageHideMsgBtn.click {
            multiPageMsgTv?.visibility = if (multiPageMsgTv?.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        multiPageMsgTv.click { multiPageMsgTv?.visibility = View.GONE }
        // 自动跳转
        multiPageAutoJumpBtn?.click {
            WxGlobalDebugger.autoJump(context)
        }
        // 查看调试配置
        multiPageLookDebugCfgBtn?.click {
            val text = JsonUtils.toJsonString(JsonUtils.toJson(WxGlobalDebugger.mWeexPageMap.values), "解析失败")
            multiPageMsgTv?.text = text
            multiPageMsgTv?.visibility = View.VISIBLE
        }
        // 查看线上配置
        multiPageLookOnlineCfgBtn?.click {
            val text = JsonUtils.toJsonString(JsonUtils.toJson(CubeWx.mWxRouter.mWeexPageMap.values), "解析失败")
            multiPageMsgTv?.text = text
            multiPageMsgTv?.visibility = View.VISIBLE
        }
        multiPageIpHostEt?.setText(WxGlobalDebugger.mWxDebugCfg.multiPageDebugHost)
        multiPageEnableSwitch?.isChecked = WxGlobalDebugger.mWxDebugCfg.multiPageDebugEnable
        // 生效
        multiPageActiveBtn?.click {
            WxGlobalDebugger.mWxDebugCfg.apply {
                multiPageDebugEnable = multiPageEnableSwitch?.isChecked ?: false
                multiPageDebugHost = multiPageIpHostEt?.text.toString()
                flush()
            }
            WxGlobalDebugger.updateFromNet()
        }
    }

    private fun updateMsg() {
        val msg = StringBuilder()
                .append("页面：").newLine()
                .append(mWeexPageDebugger.mWeexPage?.toShowString()).newLine()
                .append("信息：").newLine()
                .append(mWeexPageDebugger.mWxPageDebugCfg.toShowString()).newLine()
                .toString()
        pageMsgTv?.text = msg
        pageMsgTv?.visibility = View.VISIBLE
    }

    override fun show() {
        super.show()
        updateMsg()
    }

    /* 全部参数设置属性 */
    private fun setDialogAttributes(width: Int, height: Int, alpha: Float, dim: Float, gravity: Int) {
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        val window = window ?: return
        val params = window.attributes
        // setContentView设置布局的透明度，0为透明，1为实际颜色,该透明度会使layout里的所有空间都有透明度，不仅仅是布局最底层的view
        params.alpha = alpha
        // 窗口的背景，0为透明，1为全黑
        params.dimAmount = dim
        params.width = width
        params.height = height
        params.gravity = gravity
        window.attributes = params
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
}
