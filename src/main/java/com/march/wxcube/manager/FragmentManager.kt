package com.march.wxcube.manager

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import com.march.common.utils.LogUtils
import com.march.wxcube.model.FragmentConfig

/**
 * CreateAt : 2018/3/29
 * Describe : 负责处理页面内多个模块
 *
 * @author chendong
 */
class FragmentManager(private val mFragmentManager: FragmentManager,
                      private val mConfigs: List<FragmentConfig>,
                      private val mHandler: FragmentHandler) : BaseManager() {


    override fun onViewCreated() {
        LogUtils.e("FragmentManager onViewCreated")
        checkPreloadFragment()
        val config = mConfigs.firstOrNull { it.indexPage }
        showFragment(config?.tag)
    }

    /**
     * 显示某个fragment
     */
    fun showFragment(showTag: String?) {
        if (showTag == null) {
            return
        }
        performSelectItem(showTag)
    }

    /**
     * 准备需要提前加载的 fragment,但是不显示
     */
    private fun checkPreloadFragment() {
        prepareContainerId()
        LogUtils.e("checkPreloadFragment $containerId")
        if (containerId == -1 || containerId == 0) {
            return
        }
        val beginTransaction = mFragmentManager.beginTransaction()
        mConfigs.filter { !it.lazyLoad }
                .forEach {
                    val tag = it.tag!!
                    val fragment = mHandler.makeFragment(tag)
                    LogUtils.e("checkPreloadFragment $fragment")
                    if (fragment != null) {
                        beginTransaction.add(containerId, fragment, tag).hide(fragment)
                    }
                }
        // 同步 commit 避免 getFragments 拿不到
        beginTransaction.commitNow()
    }

    private var containerId: Int = 0

    private fun prepareContainerId() {
        // 没查找过，查找一次
        if (containerId == 0) {
            containerId = mHandler.containerIdFinder().invoke()
        }
    }

    private fun performSelectItem(showTag: String) {
        prepareContainerId()
        // 查找过没找到
        if (containerId == -1 || containerId == 0) {
            return
        }
        val transaction = mFragmentManager.beginTransaction()
        var fragment: Fragment?
        val fragments = mFragmentManager.fragments
        // 第一次创建，一个都没有，不需要隐藏，直接显示
        if (fragments == null || fragments.size == 0) {
            fragment = mHandler.makeFragment(showTag)
            if (fragment != null) {
                transaction.add(containerId, fragment, showTag)
            }
        } else {
            fragment = mFragmentManager.findFragmentByTag(showTag)
            for (f in fragments) {
                if (fragment == null || fragment != f) {
                    transaction.hide(f)
                }
            }
            if (fragment == null) {
                fragment = mHandler.makeFragment(showTag)
                if (fragment != null) {
                    transaction.add(containerId, fragment, showTag)
                }
            }
        }
        if (fragment != null) {
            transaction.show(fragment).commit()
        }
    }


    interface FragmentHandler {

        fun containerIdFinder(): () -> Int

        fun makeFragment(tag: String): Fragment?
    }
}
