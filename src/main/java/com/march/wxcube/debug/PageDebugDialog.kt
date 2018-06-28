package com.march.wxcube.debug

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import com.march.common.utils.JsonUtils
import com.march.common.utils.ToastUtils
import com.march.wxcube.R
import com.march.wxcube.CubeWx
import com.march.wxcube.common.click
import com.march.wxcube.common.newLine

/**
 * CreateAt : 2018/6/17
 * Describe :
 *
 * @author chendong
 */
class PageDebugDialog(context: Context, private val mWeexPageDebugger: WxPageDebugger)
    : AppCompatDialog(context, R.style.dialog_theme) {

    init {
        setContentView(R.layout.debug_dialog)
    }

    private val hideMsgBtn by lazy { findViewById<Button>(R.id.hide_msg_btn) }
    private val descTv by lazy { findViewById<TextView>(R.id.desc_tv) }
    private val cfgTv by lazy { findViewById<TextView>(R.id.config_tv) }
    private val hideCfgBtn by lazy { findViewById<TextView>(R.id.mp_hide_cfg) }
    private val refreshJsSw by lazy { findViewById<Switch>(R.id.debug_local_js_sw) }
    private val mpEnableSw by lazy { findViewById<Switch>(R.id.mp_enable) }
    private val mpIpEt by lazy { findViewById<EditText>(R.id.mp_ip) }
    private val contentEt by lazy { findViewById<EditText>(R.id.content_et) }

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
        findViewById<View>(R.id.info_btn).click {
            ToastUtils.showLong("""
                    1. 长按可以触发强制刷新
                    2. 当在实时刷新时，图标会保持旋转作为提示
                """.trimIndent())
        }
        // 关闭
        findViewById<View>(R.id.close_btn).click { dismiss() }
    }

    // 页面调试
    private fun initCurPageDebug() {
        hideMsgBtn.click {
            if (descTv?.visibility == View.GONE) {
                descTv?.visibility = View.VISIBLE
            } else {
                descTv?.visibility = View.GONE
            }
        }
        updateMsg()
        val jsInCacheSw = findViewById<Switch>(R.id.js_in_cache_sw)
        val debugConfig = mWeexPageDebugger.mDebugConfig
        jsInCacheSw?.isChecked = debugConfig.debugJsInCache
        jsInCacheSw?.setOnCheckedChangeListener { _, isChecked ->
            debugConfig.debugJsInCache = isChecked
        }
        val jsInDiskSw = findViewById<Switch>(R.id.js_in_disk_sw)
        jsInDiskSw?.isChecked = debugConfig.debugJsInDisk
        jsInDiskSw?.setOnCheckedChangeListener { _, isChecked ->
            debugConfig.debugJsInDisk = isChecked
        }
        refreshJsSw?.isChecked = debugConfig.isRefreshRemoteJs
        refreshJsSw?.setOnCheckedChangeListener { _, isChecked ->
            debugConfig.isRefreshRemoteJs = isChecked
            if (debugConfig.isRefreshRemoteJs) {
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
        // 请求线上配置
        findViewById<View>(R.id.req_online_cfg_btn).click { CubeWx.mWxUpdater.update(context) }
        // 请求调试配置
        findViewById<View>(R.id.req_debug_cfg_btn).click { WxGlobalDebugger.updateFromNet() }
        // 清理缓存的js
        findViewById<View>(R.id.clear_cache_btn).click { CubeWx.mWxJsLoader.clearCache() }
        // 清理磁盘js
        findViewById<View>(R.id.clear_disk_btn).click { CubeWx.clearDiskCache() }
        findViewById<View>(R.id.jump_weex_btn).click {
            val text = contentEt?.text.toString()
            if (!text.isBlank()) {
                CubeWx.mWxRouter.openUrl(context, text)
            }
        }
        findViewById<View>(R.id.jump_web_btn).click {
            val text = contentEt?.text.toString()
            if (!text.isBlank()) {
                CubeWx.mWxRouter.openWeb(context, text)
            }
        }
    }

    // 多页面调试
    private fun initMultiPageDebug() {
        hideCfgBtn.click {
            if (cfgTv?.visibility == View.GONE) {
                cfgTv?.visibility = View.VISIBLE
            } else {
                cfgTv?.visibility = View.GONE
            }
        }
        cfgTv.click {
            cfgTv?.visibility = View.GONE
        }
        // 自动跳转
        findViewById<View>(R.id.mp_auto_jump).click {
            WxGlobalDebugger.autoJump(context)
        }
        // 查看调试配置
        findViewById<View>(R.id.mp_look_debug_cfg).click {
            val text = JsonUtils.toJsonString(JsonUtils.toJson(WxGlobalDebugger.mWeexPageMap.values), "解析失败")
            cfgTv?.text = text
            cfgTv?.visibility = View.VISIBLE
        }
        // 查看线上配置
        findViewById<View>(R.id.mp_look_online_cfg).click {
            val text = JsonUtils.toJsonString(JsonUtils.toJson(CubeWx.mWxRouter.mWeexPageMap.values), "解析失败")
            cfgTv?.text = text
            cfgTv?.visibility = View.VISIBLE
        }
        mpIpEt?.setText(WxGlobalDebugger.getDebugHost())
        mpEnableSw?.isChecked = WxGlobalDebugger.getDebugEnable()
        // 生效
        findViewById<View>(R.id.mp_active).click {
            WxGlobalDebugger.setDebugEnable(mpEnableSw?.isChecked ?: false)
            WxGlobalDebugger.setDebugHost(mpIpEt?.text.toString())
            WxGlobalDebugger.updateFromNet()
        }
    }

    private fun updateMsg() {
        val msg = StringBuilder()
                .append("页面：").newLine()
                .append(mWeexPageDebugger.mWeexPage?.toShowString()).newLine()
                .append("信息：").newLine()
                .append(mWeexPageDebugger.mDebugMsg.toShowString()).newLine()
                .toString()
        descTv?.text = msg
        descTv?.visibility = View.VISIBLE
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
