package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import com.march.wxcube.Weex
import com.taobao.weex.common.Constants.Name.Recycler.TYPE_INDEX


/**
 * CreateAt : 2018/4/24
 * Describe :
 *
 * @author chendong
 */
class IndexActivity : WxBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Weex.getInst().mWeexInjector.onPageCreated(this,Weex.PAGE_INDEX)
        Weex.getInst().mWeexUpdater.requestPages(true, this)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }
}