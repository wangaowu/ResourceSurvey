package com.bytemiracle.resourcesurvey.common.dbbean;

import org.gdal.osr.SpatialReference;

/**
 * 类功能：应用内的坐标系处理类
 *
 * @author gwwang
 * @date 2021/6/16 14:19
 */
public class SpatialSystemProcessor {

    private SpatialReference gdalSpatialReference;
    private String wkText;

    public SpatialSystemProcessor(String wkText) {
        this.gdalSpatialReference = new SpatialReference(wkText);
        this.wkText = wkText;
    }

    /**
     * 获取短的显示名称
     *
     * @return
     */
    public String getDisplayName() {
        return gdalSpatialReference.GetAttrValue("GEOGCS");
    }

    @Override
    public String toString() {
        return wkText;
    }
}
