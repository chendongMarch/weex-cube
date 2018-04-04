package com.march.wxcube.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.march.wxcube.R
import com.march.wxcube.model.WeexPage

/**
 * CreateAt : 2018/3/19
 * Describe : Fragment 容器
 *
 * @author chendong
 */
class WeexFragment : Fragment() {

    companion object {

        fun newInstance(bundle: WeexPage): WeexFragment {
            val args = Bundle()
            args.putParcelable(WeexPage.KEY_PAGE, bundle)
            val fragment = WeexFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mContainerView: ViewGroup? = null

    private val weexDelegate: WeexDelegate by lazy {
        WeexDelegate(this, object : WeexRender.RenderService {
            override fun onViewCreated(view: View) {
                mContainerView?.addView(view)
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        weexDelegate.onCreate()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContainerView = inflater?.inflate(R.layout.weex_fragment, container, false) as ViewGroup
        weexDelegate.render()
        return mContainerView
    }

    override fun onResume() {
        super.onResume()
        weexDelegate.onResume()
    }


    override fun onPause() {
        super.onPause()
        weexDelegate.onPause()

    }

    override fun onStart() {
        super.onStart()
        weexDelegate.onStart()
    }

    override fun onStop() {
        super.onStop()
        weexDelegate.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        weexDelegate.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        weexDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
