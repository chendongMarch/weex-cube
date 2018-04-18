package com.march.wxcube.module;

import com.march.common.utils.ToastUtils;
import com.taobao.weex.annotation.JSMethod;

/**
 * CreateAt : 2018/4/17
 * Describe : 模态
 *
 * @author chendong
 */
public class ModalModule extends BaseModule {

    @JSMethod
    public void toast(String msg){
        ToastUtils.show(msg);
    }

    @JSMethod
    public void toastLong(String msg){
        ToastUtils.showLong(msg);
    }
}
