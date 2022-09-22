package org.osmdroid.customImpl.geopackage.load

import mil.nga.geopackage.features.user.FeatureRow
import mil.nga.sf.GeometryType
import org.osmdroid.overlay.bean.PackageOverlayInfo

/**
 * featureTable的查询结果封装
 * @var geometryType 图形类
 * @var featureRows  行数据
 */
data class FeatureTableResultDTO(
    var geometryType: PackageOverlayInfo.OSMGeometryType,
    var featureRows: List<FeatureRow>
) {

    companion object {
        /**
         * 使用gpkg图形类型匹配osm的自定义图形类型
         */
        fun matchOSMGeometryType(geometryType: GeometryType): PackageOverlayInfo.OSMGeometryType {
            when (geometryType) {
                GeometryType.POINT -> {
                    return PackageOverlayInfo.OSMGeometryType.POINT
                }
                GeometryType.MULTIPOINT -> {
                    return PackageOverlayInfo.OSMGeometryType.POINT
                }
                GeometryType.LINESTRING -> {
                    return PackageOverlayInfo.OSMGeometryType.LINESTRING
                }
                GeometryType.MULTILINESTRING -> {
                    return PackageOverlayInfo.OSMGeometryType.LINESTRING
                }
                GeometryType.MULTIPOLYGON -> {
                    return PackageOverlayInfo.OSMGeometryType.POLYGON
                }
                GeometryType.POLYGON -> {
                    return PackageOverlayInfo.OSMGeometryType.POLYGON
                }
                else -> {
                    return PackageOverlayInfo.OSMGeometryType.POLYGON
                }
            }
        }
    }
}