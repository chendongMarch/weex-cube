package com.march.wxcube.func.update

import android.content.Context
import com.march.common.Common
import com.march.wxcube.CubeWx
import com.march.wxcube.common.log
import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/29
 * Describe :
 *
 * @author chendong
 */
object PageFilter {

    fun filter(context: Context, pages: List<WxPage>, prepare: (List<WxPage>) -> Unit = {}): List<WxPage> {
        // return filterBestPages(pages)
        return filterBestPagesV2(context, pages, prepare)
    }

    // 过滤，页面数据OK，版本支持OK，VersionCode 合法
    private fun filterAllValidPages(pages: List<WxPage>): MutableList<WxPage> {
        val resultPages = mutableListOf<WxPage>()
        val curVersionCodes = getVersionCodes(Common.getInst().buildConfig.VERSION_NAME)
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
            compareVersion(curVersionCodes, appVersionCodes) >= 0
        }
        return resultPages
    }

    // 根据 js 版本/资源是否存在，提取每个页面最优的配置
    private fun filterBestPages(pages: List<WxPage>): List<WxPage> {
        val resultPages = filterAllValidPages(pages)
        val pageNameWxPageMap = mutableMapOf<String, WxPage>()
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
                if (compareVersion(curJsVersionCodes, lastJsVersionCodes) >= 0) {
                    pageNameWxPageMap[pageName] = it
                }
            }
        }
        resultPages.clear()
        resultPages.addAll(pageNameWxPageMap.values)
        return resultPages
    }


    // 生成 Map<pageName,List<WxPage>> 配置表, 表示某个页面下的所有配置项
    // 配置项按照 jsVersion 版本由高到低排序
    // 从高版本开始查找，搜索资源已经存在的配置项，称为有效配置项
    // 找到有效配置，中断检索，加入正式配置列表
    // 找到无效配置（版本高，但资源没有下载），加入准备配置列表，后续下载，继续检索
    private fun filterBestPagesV2(context: Context, pages: List<WxPage>, prepare: (List<WxPage>) -> Unit = {}): List<WxPage> {
        val pageCfgsMap = mutableMapOf<String, MutableSet<WxPage>>()
        val validPages = filterAllValidPages(pages)
        val needPreparePages = mutableListOf<WxPage>()
        val resultPages = mutableListOf<WxPage>()
        validPages.forEach { page ->
            page.pageName?.let { pageName ->
                val set = pageCfgsMap[pageName] ?: mutableSetOf()
                set.add(page)
                pageCfgsMap[pageName] = set
            }
        }
        for ((_, cfgs) in pageCfgsMap) {
            // 先对 cfgs 按照 js 版本排序
            val sortCfgs = cfgs.sortedWith(Comparator { last, cur ->
                val lastJsVersionCodes = getVersionCodes(last.jsVersion)
                val curJsVersionCodes = getVersionCodes(cur.jsVersion)
                compareVersion(curJsVersionCodes, lastJsVersionCodes)
            })
            var add2ResultPage: WxPage? = null
            // 从高版本开始，找到每个页面最优的版本，正式版本时需要资源存在
            for (page in sortCfgs) {
                if (CubeWx.mWxCfg.loadJsSafeMode) {
                    if (isResourceExist(context, page)) {
                        add2ResultPage = page
                        break
                    } else {
                        // LgUtils.e("chendong", "prepare ${page.toSimpleString()}")
                        needPreparePages.add(page)
                        continue
                    }
                } else {
                    // 调试时直接使用最优版本
                    resultPages.add(page)
                    break
                }
            }
            when {
                add2ResultPage != null -> resultPages.add(add2ResultPage)
                    // log( "result success ${add2ResultPage.toSimpleString()}")
                sortCfgs.isNotEmpty()  -> resultPages.add(sortCfgs[0])
                    // log("result fail ${sortCfgs[0].toSimpleString()}")
                else                   -> {
                    // log( "result not found")
                }
            }
        }
        prepare(needPreparePages)
        return resultPages
    }

    // 生产环境，所有加载的资源必须保证在本地有，否则降级
    private fun isResourceExist(context: Context, page: WxPage): Boolean {
        // 检索 assets
        var exist = CubeWx.mWxJsLoader.isAssetsJsExist(context, page)
        if (exist) {
            log("assets 存在")
        }
        // 检索文件
        if (!exist) {
            exist = CubeWx.mWxJsLoader.isLocalJsExist(page)
            if (exist) {
                log("文件 存在")
            }
        }
        return exist
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


    private fun compareVersion(v1s: List<Int>, v2s: List<Int>): Int {
        var result = 0
        for (index in 0..2) {
            if (v1s[index] > v2s[index]) {
                result = 1
                break
            } else if (v1s[index] < v2s[index]) {
                result = -1
                break
            }
        }
        return result
    }


    private fun compareVersion(v1: String, v2: String): Int {
        val v1s = getVersionCodes(v1)
        val v2s = getVersionCodes(v2)
        if (v1s.size != 3 || v2s.size != 3) {
            return -1000
        }
        return compareVersion(v1s, v2s)
    }
}