package com.bytemiracle.resourcesurvey.modules.datamanage;

import com.bytemiracle.base.framework.view.BaseCheckPojo;

import mil.nga.geopackage.features.user.FeatureColumn;

/**
 * 类功能：可选中的列数据封装
 *
 * @author gwwang
 * @date 2022/7/4 11:04
 */
public class CheckableFeatureColumn extends BaseCheckPojo {

    public FeatureColumn data;

    public CheckableFeatureColumn(FeatureColumn data, boolean isChecked) {
        this.data = data;
        super.setChecked(isChecked);
    }
}
