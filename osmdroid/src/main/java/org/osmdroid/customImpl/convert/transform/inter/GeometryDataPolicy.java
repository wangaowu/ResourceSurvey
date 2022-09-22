package org.osmdroid.customImpl.convert.transform.inter;

import org.osmdroid.customImpl.convert.transform.SpatialReferenceCompact;

/**
 * 类功能：图形数据转换策略
 *
 * @author gwwang
 * @date 2022/3/1 13:23
 */
public interface GeometryDataPolicy {
    /**
     * 将shpGeometry的坐标数据转换为wgs84的坐标
     *
     * @param srcGeometryWkt           待转换geometry的wkt
     * @param geometrySpatialReference 待转换geometry的坐标系
     * @return target坐标系下的geometryWkt
     */
    String getTransformedGeometryWkt(String srcGeometryWkt, SpatialReferenceCompact geometrySpatialReference);
}
