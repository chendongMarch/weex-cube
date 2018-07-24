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
import com.march.wxcube.common.WxUtils
import com.march.wxcube.common.downloadImage
import com.march.wxcube.common.getDef
import com.march.wxcube.common.toListEx
import com.march.wxcube.model.FragmentConfig
import com.march.wxcube.module.*
import com.march.wxcube.performer.FragmentPerformer
import com.march.wxcube.ui.WxFragment
import com.taobao.weex.adapter.URIAdapter

/**
 * CreateAt : 2018/6/7
 * Describe : 页面
 *
 * @author chendong
 */
class PageDispatcher(val module: BridgeModule) : BaseDispatcher() {

    /**
     * 初始化页面
     */
    @DispatcherJsMethod(async = true)
    fun initPage(param: DispatcherParam) {
        val activity = mProvider.activity()
        val background = param.params.getJSONObject("background")
        val containerView = module.mWeexDelegate?.mContainerView ?: throw RuntimeException("Page#initPage containerView is null")
        if (background != null) {
            val color = background.getString("color")
            color?.let {
                containerView.setBackgroundColor(ColorUtils.parseColor(it, Color.WHITE))
            }
            val image = background.getString("image")
            image?.let {
                val repeat = background.getDef("repeat", false)
                val widthScale = background.getDef("widthScale", 1f)
                val aspectRatio = background.getDef("aspectRatio", 1f)
                val imgUrl = WxUtils.rewriteUrl(image, URIAdapter.IMAGE)
                activity.downloadImage(imgUrl) {
                    containerView.background = if (repeat) {
                        DrawableUtils.newRepeatXYDrawable(activity, it, DimensUtils.WIDTH, widthScale, aspectRatio)
                    } else {
                        BitmapDrawable(activity.resources, it)
                    }
                }
            }
        }
        val interceptBack = param.params.getDef("interceptBack", false)
        module.mWeexDelegate?.mInterceptBackPressed = interceptBack
    }

    /**
     * 加载tab数据
     */
    @DispatcherJsMethod
    fun loadTabs(param: DispatcherParam) {
        val act = module.mWeexAct ?: throw RuntimeException("Page#loadTabs mWeexAct is null")
        val array = param.params.getJSONArray("tabs") ?: throw RuntimeException("Page#loadTabs tabs is null")
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
     */
    @DispatcherJsMethod
    fun showTab(param: DispatcherParam) {
        val act = module.mWeexAct ?: throw RuntimeException("Page#loadTabs mWeexAct is null")
        val tag = param.params.getString(KEY_TAG) ?: throw RuntimeException("Page#showTab tag is null")
        val performer = act.mDelegate.getPerformer(FragmentPerformer::class.java)
                ?: throw RuntimeException("Page#showTab performer is null")
        performer.showFragment(tag)
    }

    /**
     * 刷新
     */
    @DispatcherJsMethod
    fun reloadPage() {
        module.mWeexDelegate?.render()
    }
}