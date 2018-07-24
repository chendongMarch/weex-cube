package com.march.wxcube.module

import com.alibaba.fastjson.JSONObject
import com.march.wxcube.module.dispatcher.BaseDispatcher
import java.lang.reflect.Method

/**
 * CreateAt : 2018/7/24
 * Describe : 辅助自动注册和调用方法
 *
 * @author chendong
 */

// 方法注解
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DispatcherJsMethod(
        val UI: Boolean = true, // must invoke in the ui thread
        val async: Boolean = false, // a time-consuming method
        val alias: String = "" // alias for register method
)


// 方法调用参数
data class DispatcherParam(
        val method: String,
        val params: JSONObject,
        val callback: Callback
)

// 方法对象
data class DispatcherMethod(
        val UI: Boolean,
        val async: Boolean,
        val name: String,
        val method: Method,
        val dispatcher: BaseDispatcher
) {

    override fun equals(other: Any?): Boolean {
        return other is DispatcherMethod && name == other.name
    }

    override fun hashCode(): Int {
        return 43
    }

    override fun toString(): String {
        return "DispatcherMethod(UI=$UI, async=$async, name='$name')"
    }


}