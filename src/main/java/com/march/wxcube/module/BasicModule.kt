package com.march.wxcube.module

import android.support.v4.app.Fragment
import android.text.TextUtils

import com.alibaba.fastjson.JSONArray
import com.march.wxcube.R
import com.march.wxcube.Weex
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.model.FragmentConfig
import com.march.wxcube.ui.WeexDialogFragment
import com.march.wxcube.ui.WeexFragment
import com.march.wxcube.utils.FragmentUtils
import com.taobao.weex.annotation.JSMethod


/**
 * CreateAt : 2018/3/28
 * Describe :
 *
 * @author chendong
 */
class BasicModule : BaseModule() {

    @JSMethod
    fun openUrl(webUrl: String) {
        val ctx = context ?: return
        Weex.getInst().weexRouter.openUrl(ctx, webUrl)
    }

    @JSMethod
    fun openDialog(webUrl: String, params: Map<String, Any>) {
        val act = activity ?: return
        val config = map2Obj(params, DialogConfig::class.java)
        val page = Weex.getInst().weexRouter.findPage(webUrl) ?: return
        val fragment = WeexDialogFragment.newInstance(page, config)
        fragment.show(act.supportFragmentManager, "dialog")
    }

    @JSMethod
    fun loadTabPages(array: JSONArray) {
        val weexAct = weexActivity ?: return
        val configs = jsonArray2List(array, FragmentConfig::class.java)

        val utils = FragmentUtils(weexAct.supportFragmentManager, object : FragmentUtils.FragmentHandler {
            override val fragmentContainerId: Int
                get() = R.id.weex_activity_root

            override fun makeFragment(tag: String): Fragment? {
                val config = configs.firstOrNull { it.tag.equals(tag) }
                if (config != null && !TextUtils.isEmpty(config.url)) {
                    val page = Weex.getInst().weexRouter.findPage(config.url!!)
                    page ?: return null
                    return WeexFragment.newInstance(page)
                }
                return null
            }
        })
        weexAct.weexDelegate.extra["fragment-manager"] = utils
    }

    @JSMethod
    fun showTab(tag: String) {
        val weexAct = weexActivity ?: return
        val obj = weexAct.weexDelegate.extra["fragment-manager"] as? FragmentUtils ?: return
        obj.showFragment(tag)
    }
}
