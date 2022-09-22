package org.osmdroid.overlay.utils;

import org.osmdroid.util.BoundingBox;

/**
 * 类功能：地图的常量定义类
 *
 * @author gwwang
 * @date 2022/2/23 13:20
 */
public interface MapConstant {

    //数据边界
    double MinLatitude = -85.05112877980658;
    double MaxLatitude = 85.05112877980658;
    double MinLongitude = -180;
    double MaxLongitude = 180;

    //tileOverlay的标记
    String OVERLAY_TIAN_DI_TU_IMAGE = "天地图影像";
    String OVERLAY_TIAN_DI_TU_CIA = "天地图注记";

    //overlay的标记
    String TAG_TRAJECTORY = "trajectory_overlay";
    String TAG_MY_POINT = "my_point_overlay";
    String TAG_GEOMETRY = "graphic_overlay";
    String TAG_GEOPACKAGE = "geopackage_overlay";
    String TAG_SHP = "shp_overlay";


    // zoom_level   4--->18
    double MIN_ZOOM_LEVEL_CHINA = 4.0;
    double MAX_ZOOM_LEVEL = 19.0;
    double LOCATION_ZOOM_LEVEL = MAX_ZOOM_LEVEL - 1;
    // china--> N:48.34956647807995; E:142.02880429281817; S:16.6070368345581; W:62.013882523468055
    BoundingBox BOUNDING_CHINA = new BoundingBox(48.34956647807995, 142.02880429281817, 16.6070368345581, 62.013882523468055);

    //缩放时，图形距离屏幕的边距
    int DEFAULT_BOX_PADDING = 180;

    //响应点击时的宽容度
    int INVOKE_CLICK_TOLERANCE = 10;
}
