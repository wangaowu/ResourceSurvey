package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz;

import com.bytemiracle.base.framework.view.BaseCheckPojo;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/11 9:30
 */
public class SelectLayerName extends BaseCheckPojo {
    public String name;

    public SelectLayerName(String name, boolean checked) {
        this.name = name;
        super.setChecked(checked);
    }
}
