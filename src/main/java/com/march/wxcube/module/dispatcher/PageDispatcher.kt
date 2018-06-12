package com.march.wxcube.module.dispatcher

import android.app.Activity
import android.graphics.Color
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.ViewGroup
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import com.march.wxcube.common.getDef
import com.march.wxcube.common.toListEx
import com.march.wxcube.model.FragmentConfig
import com.march.wxcube.module.OneModule
import com.march.wxcube.module.findView
import com.march.wxcube.module.mWeexAct
import com.march.wxcube.module.mWeexDelegate
import com.march.wxcube.performer.FragmentPerformer
import com.march.wxcube.ui.WeexActivity
import com.march.wxcube.ui.WeexFragment
import com.march.wxcube.module.JsCallbackWrap

/**
 * CreateAt : 2018/6/7
 * Describe :
 *
 * @author chendong
 */
class PageDispatcher(val module: OneModule) : BaseDispatcher() {
    
    companion object {
        const val initPage = "initPage"
        const val loadTabs = "loadTabs"
        const val showTab = "showTab"
        const val reloadPage = "reloadPage"
    }

    override fun dispatch(method: String, params: JSONObject, jsCallbackWrap: JsCallbackWrap) {
        val weexAct = module.mWeexAct ?: throw RuntimeException("Page#loadTabs mWeexAct is null")
        when (method) {
            initPage   -> initPage(weexAct, params)
            loadTabs   -> loadTabs(weexAct, params)
            showTab    -> showTab(weexAct, params)
            reloadPage -> reloadPage()
        }
    }

    override fun getMethods(): Array<String> {
        return arrayOf(initPage, loadTabs, showTab)
    }

    /**
     * 初始化页面
     */
    private fun initPage(act: Activity, params: JSONObject) {
        val color = try {
            Color.parseColor(params.getString("bgColor") ?: "#ffffff")
        } catch (e: Exception) {
            Color.WHITE
        }
        module.mWeexDelegate?.mContainerView?.setBackgroundColor(color)
        val back = params.getDef("back", false)
        if(back) {
            module.mWeexDelegate?.mHandleBackPressed = true
        }
    }

    /**
     * 加载tab数据
     */
    private fun loadTabs(act: WeexActivity, params: JSONObject) {
        val array = params.getJSONArray("tabs") ?: throw RuntimeException("Page#loadTabs tabs is null")
        val configs = array.toListEx(FragmentConfig::class.java) ?: throw RuntimeException("Page#loadTabs mWeexAct is null")
        act.mDelegate.addPerformer(FragmentPerformer(act.supportFragmentManager,
                configs, object : FragmentPerformer.FragmentHandler {
            override fun containerIdFinder(): () -> Int {
                return {
                    val view = module.findView { it is ViewGroup && it.tag == "container" }
                    if (view !is ViewGroup) {
                        -1
                    }
                    view?.id ?: -1
                }
            }

            override fun makeFragment(tag: String): Fragment? {
                val config = configs.firstOrNull { it.tag.equals(tag) }
                if (config != null && !TextUtils.isEmpty(config.url)) {
                    val page = Weex.getInst().mWeexRouter.findPage(config.url!!) ?: return null
                    return WeexFragment.newInstance(page)
                }
                return null
            }
        }))

    }

    /**
     * 显示 tab
     * @param tag tab 对应的 tag
     */
    private fun showTab(act: WeexActivity, params: JSONObject) {
        val tag = params.getString(KEY_TAG) ?: throw RuntimeException("Page#showTab tag is null")
        val performer = act.mDelegate.getPerformer(FragmentPerformer::class.java)
                ?: throw RuntimeException("Page#showTab performer is null")
        performer.showFragment(tag)
    }

    /**
     * 刷新
     */
    private fun reloadPage() {
        module.mWeexDelegate?.render()
    }
}