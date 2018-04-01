package com.march.wxcube

import com.taobao.weex.InitConfig

/**
 * CreateAt : 2018/4/1
 * Describe :
 *
 * @author chendong
 */
interface WeexService {


    fun onErrorReport(throwable: Throwable?, errorMsg: String)

    fun onLog(tag: String, msg: String)

    fun onInitWeex(builder: InitConfig.Builder)

    companion object {
        val EMPTY = object : WeexService {
            override fun onErrorReport(throwable: Throwable?, errorMsg: String) {

            }

            override fun onLog(tag: String, msg: String) {
            }

            override fun onInitWeex(builder: InitConfig.Builder) {
            }
        }
    }

}