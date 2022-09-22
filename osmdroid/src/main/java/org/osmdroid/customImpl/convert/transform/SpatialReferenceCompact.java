package org.osmdroid.customImpl.convert.transform;

import org.gdal.osr.SpatialReference;

/**
 * 类功能：esri和ogc的坐标系适配类
 *
 * @author gwwang
 * @date 2022/3/26 15:44
 */
public class SpatialReferenceCompact {

    private SpatialReference spatialReference;

    /**
     * 构造方法 ogc或者esri的wkt
     *
     * @param spatialReferenceWkt
     */
    public SpatialReferenceCompact(String spatialReferenceWkt) {
        this(new SpatialReference(spatialReferenceWkt));
    }

    /**
     * 构造方法 ogc或者esri构造的对象
     *
     * @param spatialReference
     */
    public SpatialReferenceCompact(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    /**
     * 获取esri的坐标系wkt
     *
     * @return
     */
    public String getEsriSpatialReferenceWkt() {
        spatialReference.MorphToESRI();
        return spatialReference.ExportToWkt();
    }

    /**
     * 获取ogc的坐标系wkt
     *
     * @return
     */
    public String getOGCSpatialReferenceWkt() {
        spatialReference.MorphFromESRI();
        return spatialReference.ExportToWkt();
    }

    /**
     * 获取坐标系的proj4的参数
     *
     * @return
     */
    public String getSpatialReferenceProj4() {
        spatialReference.MorphFromESRI();
        return spatialReference.ExportToProj4();
    }
}
