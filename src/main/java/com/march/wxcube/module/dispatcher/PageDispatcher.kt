package com.march.wxcube.module.dispatcher

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.ViewGroup
import com.alibaba.fastjson.JSONObject
import com.march.common.utils.ColorUtils
import com.march.common.utils.DimensUtils
import com.march.common.utils.DrawableUtils
import com.march.wxcube.CubeWx
import com.march.wxcube.common.downloadImage
import com.march.wxcube.common.getDef
import com.march.wxcube.common.toListEx
import com.march.wxcube.model.FragmentConfig
import com.march.wxcube.module.*
import com.march.wxcube.performer.FragmentPerformer
import com.march.wxcube.ui.WxActivity
import com.march.wxcube.ui.WxFragment

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
            initPage   -> initPage(params)
            loadTabs   -> loadTabs(weexAct, params)
            showTab    -> showTab(weexAct, params)
            reloadPage -> reloadPage()
        }
    }

    override fun getAsyncMethods(): Array<String> {
        return arrayOf(initPage)
    }

    override fun getMethods(): Array<String> {
        return arrayOf(initPage, loadTabs, showTab, reloadPage)
    }
//
//    private fun goHome(method: String, params: JSONObject) {
//        mProvider.doBySelf(method, params)
//    }

    /**
     * 初始化页面
     */
    private fun initPage(params: JSONObject) {
        val activity = mProvider.activity()
        val background = params.getJSONObject("background")
        val containerView = module.mWeexDelegate?.mContainerView ?: throw RuntimeException("Page#initPage containerView is null")
        if (background != null) {
            val image = background.getString("image")
            if (image != null) {
                val repeat = background.getDef("repeat", false)
                val widthScale = background.getDef("widthScale", 1f)
                val aspectRatio = background.getDef("aspectRatio", 1f)
                activity.downloadImage(image) {
                    containerView.background = if (repeat) {
                        DrawableUtils.newRepeatXYDrawable(activity, it, DimensUtils.WIDTH, widthScale, aspectRatio)
                    } else {
                        BitmapDrawable(activity.resources, it)
                    }
                }
            } else {
                val color = background.getString("color")
                containerView.setBackgroundColor(ColorUtils.parseColor(color, Color.WHITE))
            }
        }
        val interceptBack = params.getDef("interceptBack", false)
        module.mWeexDelegate?.mInterceptBackPressed = interceptBack
    }

    /**
     * 加载tab数据
     */
    private fun loadTabs(act: WxActivity, params: JSONObject) {
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
                    val page = CubeWx.mWxRouter.findPage(config.url!!) ?: return null
                    return WxFragment.newInstance(page)
                }
                return null
            }
        }))

    }

    /**
     * 显示 tab
     * @param tag tab 对应的 tag
     */
    private fun showTab(act: WxActivity, params: JSONObject) {
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