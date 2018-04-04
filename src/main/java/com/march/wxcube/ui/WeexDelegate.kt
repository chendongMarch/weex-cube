package com.march.wxcube.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup

import com.march.wxcube.lifecycle.WeexLifeCycle
import com.march.wxcube.manager.BaseManager
import com.march.wxcube.model.WeexPage
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

    private var weexPage: WeexPage? = null
    private var weexView: ViewGroup? = null

    private var managers = mutableMapOf<String, BaseManager>()

    constructor(fragment: Fragment, renderService: WeexRender.RenderService) {
        this.weexPage = fragment.arguments.getParcelable(WeexPage.KEY_PAGE)
        init(fragment.activity, renderService)
    }

    constructor(activity: Activity, renderService: WeexRender.RenderService) {
        this.weexPage = activity.intent.getParcelableExtra(WeexPage.KEY_PAGE)
        init(activity, renderService)
    }

    private fun init(activity: Activity, renderService: WeexRender.RenderService) {
        this.actContext = activity
        this.weexInst = WXSDKInstance(actContext)
        this.weexRender = WeexRender(actContext, weexInst, object : WeexRender.RenderService {
            override fun onViewCreated(view: View) {
                weexView = view as ViewGroup
                renderService.onViewCreated(view)
                for (manager in managers) {
                    manager.value.onViewCreated(view)
                }
            }
        })
    }

    fun render() {
        if (weexPage == null) {
            return
        }
        val bundle = weexPage!!
        val opts = HashMap<String, Any>()
        val realUrl = bundle.webUrl
        if (!realUrl.isNullOrEmpty()) {
            val uri = Uri.parse(realUrl)
            val parameterNames = uri.queryParameterNames
            for (parameterName in parameterNames) {
                opts[parameterName] = uri.getQueryParameter(parameterName)
            }
        }
        weexRender.render(bundle, opts)
    }

    fun putExtra(obj: BaseManager) {
        managers[obj.javaClass.simpleName] = obj
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getExtra(clz: Class<T>): T? {
        val obj = managers[clz.simpleName]
        if (obj == null || obj.javaClass != clz) {
            return null
        }
        return obj as T
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
