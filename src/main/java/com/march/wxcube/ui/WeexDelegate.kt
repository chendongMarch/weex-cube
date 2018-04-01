package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup

import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.model.PageBundle
import com.taobao.weex.WXSDKInstance

import java.util.HashMap

/**
 * CreateAt : 2018/3/27
 * Describe : weex ui 代理
 *
 * @author chendong
 */
class WeexDelegate : WeexLifeCycle {

    private lateinit var weexRender: WeexRender
    private lateinit var weexInst: WXSDKInstance
    private lateinit var actContext: Activity

    private var pageBundle: PageBundle? = null
    private var weexView: ViewGroup? = null

    var extra = mutableMapOf<String, Any>()

    constructor(fragment: Fragment, renderService: WeexRender.RenderService) {
        this.pageBundle = fragment.arguments.getParcelable(PageBundle.KEY_PAGE)
        init(fragment.activity, renderService)
    }

    constructor(activity: Activity, renderService: WeexRender.RenderService) {
        this.pageBundle = activity.intent.getParcelableExtra(PageBundle.KEY_PAGE)
        init(activity, renderService)
    }

    private fun init(activity: Activity, renderService: WeexRender.RenderService) {
        this.actContext = activity
        this.weexInst = WXSDKInstance(actContext)
        this.weexRender = WeexRender(actContext, weexInst, object : WeexRender.RenderService {
            override fun onViewCreated(view: View) {
                weexView = view as ViewGroup
                renderService.onViewCreated(view)
            }
        })
    }

    fun render() {
        if (pageBundle == null) {
            return
        }
        val bundle = pageBundle!!
        val opts = HashMap<String, Any>()
        val realUrl = bundle.realUrl
        if (!realUrl.isNullOrEmpty()) {
            val uri = Uri.parse(realUrl)
            val parameterNames = uri.queryParameterNames
            for (parameterName in parameterNames) {
                opts[parameterName] = uri.getQueryParameter(parameterName)
            }
        }
        weexRender.render(bundle, opts)
    }

    override fun onCreate() {
        weexInst.onActivityCreate()
    }

    override fun onStart() {
        weexInst.onActivityStart()
    }

    override fun onResume() {
        weexInst.onActivityResume()
    }

    override fun onPause() {
        weexInst.onActivityPause()
    }

    override fun onStop() {
        weexInst.onActivityStop()
    }

    override fun onDestroy() {
        weexInst.onActivityDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        weexInst.onActivityResult(requestCode, resultCode, data)
    }
}
