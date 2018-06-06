package com.march.wxcube.module

import android.support.v4.app.Fragment
import android.text.TextUtils
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.march.wxcube.Weex
import com.march.wxcube.manager.ManagerRegistry
import com.march.wxcube.model.DialogConfig
import com.march.wxcube.model.FragmentConfig
import com.march.wxcube.performer.FragmentPerformer
import com.march.wxcube.ui.WeexFragment
import com.taobao.weex.annotation.JSMethod
import com.taobao.weex.bridge.JSCallback
import com.taobao.weex.common.WXModule


/**
 * CreateAt : 2018/3/28
 * Describe : 基础 module
 *
 * @author chendong
 */
class BasicModule : WXModule() {

    companion object {
        const val KEY = "cube-basic"
    }

    /**
     * 读取 mInstId
     */
    @JSMethod(uiThread = true)
    fun readInstanceId(jsCallback: JSCallback) {
        jsCallback.invoke(mWXSDKInstance.instanceId)
    }

    /**
     * 针对某个 url 存数据
     */
    @JSMethod(uiThread = true)
    fun putExtraData(url: String, data: JSONObject) {
        ManagerRegistry.DATA.putData(url, data)
    }

    /**
     * 关闭页面，dialog 会 dismiss
     */
    @JSMethod(uiThread = true)
    fun close() {
        val delegate = mWeexDelegate ?: return
        delegate.close()
    }

    /**
     * 打开页面
     */
    @JSMethod(uiThread = true)
    fun openUrl(webUrl: String) {
        val ctx = mCtx ?: return
        Weex.getInst().mWeexRouter.openUrl(ctx, webUrl)
    }

    /**
     * 打开弹窗
     */
    @JSMethod(uiThread = true)
    fun openDialog(webUrl: String, params: JSONObject) {
        val act = mAct ?: return
        val config = jsonObj2Obj(params, DialogConfig::class.java)
        Weex.getInst().mWeexRouter.openDialog(act, webUrl, config)
    }

    /**
     * 打开浏览器
     */
    @JSMethod(uiThread = true)
    fun openBrowser(webUrl: String) {
        Weex.getInst().mWeexRouter.openBrowser(mCtx, webUrl)
    }

    /**
     * 重新 load 这个页面
     */
    @JSMethod(uiThread = true)
    fun refresh() {
        mWeexDelegate?.render()
    }

    /**
     * 打开 web 界面
     */
    @JSMethod(uiThread = true)
    fun openWeb(webUrl: String) {
        Weex.getInst().mWeexRouter.openWeb(mCtx, webUrl)
    }

    /**
     * 加载 tab
     */
    @JSMethod(uiThread = true)
    fun loadTabs(array: JSONArray) {
        val weexAct = mWeexAct ?: return
        val configs = jsonArray2List(array, FragmentConfig::class.java)
        weexAct.mDelegate.addPerformer(FragmentPerformer(weexAct.supportFragmentManager,
                configs, object : FragmentPerformer.FragmentHandler {
            override fun containerIdFinder(): () -> Int {
                return {
                    val view = findView { it.tag == "container" }
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
    @JSMethod(uiThread = true)
    fun showTab(tag: String) {
        val weexAct = mWeexAct ?: return
        val performer = weexAct.mDelegate.getPerformer(FragmentPerformer::class.java)
                ?: return
        performer.showFragment(tag)
    }


    /**
     * 注册接受某事件
     * const event = weex.requireModule('cube-event')
     * event.registerEvent('myEvent')
     * globalEvent.addEventListener('myEvent', (params) => {});
     */
    @JSMethod(uiThread = true)
    fun registerEvent(event: String?) {
        ManagerRegistry.EVENT.registerEvent(event, mInstId)
    }


    /**
     * 发送事件
     * const event = weex.requireModule('cube-event')
     * event.postEvent('myEvent',{isOk:true});
     */
    @JSMethod(uiThread = true)
    fun postEvent(event: String, params: Map<String, Any>) {
        ManagerRegistry.EVENT.postEvent(event, params)
    }


    /**
     * 取消注册事件
     * const event = weex.requireModule('cube-event')
     * event.unRegisterEvent('myEvent');
     */
    @JSMethod(uiThread = true)
    fun unRegisterEvent(event: String) {
        ManagerRegistry.EVENT.unRegisterEvent(event, mInstId)
    }
}
