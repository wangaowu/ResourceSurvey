package com.bytemiracle.resourcesurvey.giscommon;

import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;


import mil.nga.geopackage.GeoPackage;
import mil.nga.sf.GeometryType;

/**
 * 类功能：arcgisAPI和geopackage_android的交互
 *
 * @author gwwang
 * @date 2021/7/2 10:32
 */
public class TransApi {

    /**
     * 获取Geopackage的图形类型
     *
     * @param featureTableName
     * @return
     */
    public static GeometryType getSfGeometryType(String featureTableName,GeoPackage geoPackage) {
        String projectName = GlobalObjectHolder.getOpeningProject().getName();
        String projectGeoPackage = ProjectUtils.getProjectGeoPackage(projectName);
        GeometryType geometryType = geoPackage.getFeatureDao(featureTableName).getGeometryType();
        GeoPackageQuick.sink2Database(geoPackage);
        return geometryType;
    }
}
