package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.march.wxcube.R
import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/3/19
 * Describe : Fragment 容器
 *
 * @author chendong
 */
class WxFragment : Fragment() {

    companion object {

        fun newInstance(bundle: WxPage): WxFragment {
            val args = Bundle()
            args.putParcelable(WxPage.KEY_PAGE, bundle)
            val fragment = WxFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val mDelegate: WxDelegate by lazy { WxDelegate(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDelegate.onCreate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.weex_container, container, false) as ViewGroup
        mDelegate.initContainerView(view)
        mDelegate.render()
        return view
    }

    override fun onResume() {
        super.onResume()
        mDelegate.onResume()
    }


    override fun onPause() {
        super.onPause()
        mDelegate.onPause()

    }

    override fun onStart() {
        super.onStart()
        mDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        mDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDelegate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
