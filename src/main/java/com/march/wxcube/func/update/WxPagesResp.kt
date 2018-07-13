package com.march.wxcube.func.update

import com.march.wxcube.model.WxPage

/**
 * CreateAt : 2018/6/17
 * Describe : 线上配置返回数据
 *
 * @author chendong
 */
class WxPagesResp {
    var total: Int? = 0
    var indexPage: String = ""
    var datas: MutableList<WxPage> = mutableListOf()
}