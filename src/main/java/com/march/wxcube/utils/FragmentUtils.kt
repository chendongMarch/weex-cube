package com.march.wxcube.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction

/**
 * CreateAt : 2018/3/29
 * Describe :
 *
 * @author chendong
 */
class FragmentUtils(mFragmentManager: FragmentManager, private val mHandler: FragmentHandler) {
    private var mFragmentManager: FragmentManager? = null

    init {
        this.mFragmentManager = mFragmentManager
        this.mFragmentManager = mFragmentManager
    }


    fun showFragment(showTag: String?) {
        if (showTag == null) {
            return
        }
        performSelectItem(showTag)
    }

    fun prepareFragment(tag: String) {
        val fragment = mHandler.makeFragment(tag)
        if (fragment != null) {
            mFragmentManager!!.beginTransaction()
                    .add(mHandler.fragmentContainerId, fragment, tag)
                    .commitAllowingStateLoss()
        }
    }


    private fun performSelectItem(showTag: String) {
        val transaction = mFragmentManager!!.beginTransaction()
        var fragment: Fragment?
        val fragments = mFragmentManager!!.getFragments()
        // 第一次创建，一个都没有，不需要隐藏，直接显示
        if (fragments == null || fragments!!.size == 0) {
            fragment = mHandler.makeFragment(showTag)
            if (fragment != null) {
                transaction.add(mHandler.fragmentContainerId, fragment, showTag)
            }
        } else {
            fragment = mFragmentManager!!.findFragmentByTag(showTag)
            for (f in fragments!!) {
                if (fragment == null || fragment != f) {
                    transaction.hide(f)
                }
            }
            if (fragment == null) {
                fragment = mHandler.makeFragment(showTag)
                if (fragment != null) {
                    transaction.add(mHandler.fragmentContainerId, fragment, showTag)
                }
            }
        }
        if (fragment != null) {
            transaction.show(fragment).commitAllowingStateLoss()
        }
    }


    interface FragmentHandler {

        val fragmentContainerId: Int

        fun makeFragment(tag: String): Fragment?
    }
}
