package com.march.wxcube.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.march.wxcube.R
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.model.WeexPage

/**
 * CreateAt : 2018/3/28
 * Describe : 弹窗容器
 *
 * @author chendong
 */
open class WeexDialogFragment : DialogFragment() {

    private lateinit var mConfig: DialogConfig

    private val mDelegate: WeexDelegate by lazy { WeexDelegate(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_theme)
        mDelegate.onCreate()
        mConfig = arguments?.getParcelable(DialogConfig.KEY_DIALOG_CONFIG) ?: DialogConfig()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weex_container, container, false) as ViewGroup
        mDelegate.initContainerView(view)
        mDelegate.render()
        return view
    }

    /* 全部参数设置属性 */
    private fun setDialogAttributes(dialog: Dialog, config: DialogConfig) {
        isCancelable = true
        val ctx = context?:return
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window ?: return
        val params = window.attributes
        // setContentView设置布局的透明度，0为透明，1为实际颜色,该透明度会使layout里的所有空间都有透明度，不仅仅是布局最底层的view
        params.alpha = config.alpha
        // 窗口的背景，0为透明，1为全黑
        params.dimAmount = config.dim
        params.width = config.getWidthParse(ctx)
        params.height = config.getHeightParse(ctx)
        params.gravity = config.gravityParse
        window.attributes = params
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        if (config.animParse != 0) {
            window.setWindowAnimations(config.animParse)
        }
    }


    override fun onResume() {
        super.onResume()
        mDelegate.onResume()
    }


    override fun onPause() {
        super.onPause()
        mDelegate.onPause()

    }

    override fun onStart() {
        super.onStart()
        mDelegate.onStart()
        setDialogAttributes(dialog, mConfig)
    }

    override fun onStop() {
        super.onStop()
        mDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelegate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mDelegate.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun newInstance(bundle: WeexPage, config: DialogConfig?): WeexDialogFragment {
            val args = Bundle()
            args.putParcelable(WeexPage.KEY_PAGE, bundle)
            args.putParcelable(DialogConfig.KEY_DIALOG_CONFIG, config)
            val fragment = WeexDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
