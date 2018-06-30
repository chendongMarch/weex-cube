package com.march.wxcube.update

import com.march.common.Common
import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/29
 * Describe :
 * 过滤：
 * app 版本号 小于等于 当前版本号
 *
 * @author chendong
 */
object PageFilter {
    /*
     * 过滤规则：
     * app 版本号 小于等于 当前版本号
     * 相同页面只保留 jsVersion 较高的一个
     * 资源在本地或者包内已经准备好，
     */

    fun filter(pages: List<WxPage>): List<WxPage> {
        return filterBestPages(pages)
    }

    // 过滤，页面数据OK，版本支持OK，VersionCode 合法
    private fun filterAllValidPages(pages: List<WxPage>): MutableList<WxPage> {
        val resultPages = mutableListOf<WxPage>()
        val curVersionCodes = getVersionCodes(Common.BuildConfig.VERSION_NAME)
        if (curVersionCodes.size != 3) {
            return pages.toMutableList()
        }
        pages.filterTo(resultPages) {
            if (!it.isValid) {
                return@filterTo false
            }
            val appVersionCodes = getVersionCodes(it.appVersion)
            val jsVersionCodes = getVersionCodes(it.jsVersion)
            if (jsVersionCodes.size != 3 || appVersionCodes.size != 3) {
                return@filterTo false
            }
            // 当前版本要 >= 最小支持版本
            curVersionCodes.moreThan(appVersionCodes)
        }
        return resultPages
    }

    // 根据 js 版本/资源是否存在，提取每个页面最优的配置
    private fun filterBestPages(pages: List<WxPage>): List<WxPage> {
        val resultPages = filterAllValidPages(pages)
        val pageNameWxPageMap = mutableMapOf<String, WxPage>()
        val needPreparePages = mutableListOf<WxPage>()
        // 每个页面只保留一个配置
        resultPages.forEach {
            val pageName = it.pageName ?: return@forEach
            val value: WxPage? = pageNameWxPageMap[pageName]
            if (value == null) {
                pageNameWxPageMap[pageName] = it
            } else {
                // 老页面，比较，并保留一个版本较高的
                // 比较 jsVersion，如果当前的 js 版本比较大，则选择当前的
                val curJsVersionCodes = getVersionCodes(it.jsVersion)
                val lastJsVersionCodes = getVersionCodes(value.jsVersion)
                if (curJsVersionCodes.moreThan(lastJsVersionCodes)) {
                    pageNameWxPageMap[pageName] = it
                }
            }
        }
        resultPages.clear()
        resultPages.addAll(pageNameWxPageMap.values)
        return resultPages
    }

    // 生产环境，所有加载的资源必须保证在本地有，否则降级
    private fun isResourceExist(page: WxPage): Boolean {
        return true
    }

    // 根据字符串生成 版本 int 类型列表
    private fun getVersionCodes(version: String?): List<Int> {
        if (version == null) {
            return listOf()
        }
        return try {
            version.split(".").map { it.toInt() }
        } catch (e: Exception) {
            listOf()
        }
    }

    // 当前版本大于传入版本
    // 版本的对比，如果要大于
    private fun List<Int>.moreThan(versionCodes: List<Int>): Boolean {
        return this[0] >= versionCodes[0] && this[1] >= versionCodes[1] && this[2] >= versionCodes[2]
    }

}