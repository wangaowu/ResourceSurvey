package com.bytemiracle.resourcesurvey.common.basecompunent;

/**
 * 类功能：功能冲突按钮需要实现该接口
 *
 * @author gwwang
 * @date 2021/5/22 14:26
 */
public interface IFunction {
    // 0：正常  1：点击  2：禁用
    int NORM = 0;
    int PRESSED = 1;
    int DISABLE = 2;

    void onCancel();
}
