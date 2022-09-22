package com.bytemiracle.resourcesurvey.giscommon.location;


import org.osmdroid.util.GeoPoint;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/4/9 9:03
 */
public abstract class LocationDataChangedListener {
    private static final double MINIMUM_DISTANCE = 5;//m

    /**
     * 监听位置变化（sdk回调，不做去重）
     *
     * @param point
     * @param headDirection
     */
    public void onChanged(GeoPoint point, double headDirection) {
    }

    /**
     * 监听位置变化（回调位置不一样，必须移动超过 getReflectMinimumDistance）
     *
     * @param oldPoint      移动前的位置
     * @param newPoint      移动后的位置
     * @param accuracy      容差范围
     * @param headDirection
     */
    public abstract void onProceedChanged(GeoPoint oldPoint, GeoPoint newPoint, double accuracy, double headDirection);

    /**
     * 最小的位置
     *
     * @return
     */
    public double initReflectMinimumDistance() {
        return MINIMUM_DISTANCE;
    }

    /**
     * 是初始化点
     *
     * @param point
     * @return
     */
    protected boolean isDefaultPoint(GeoPoint point) {
        return point == null || (point.getLongitude() == 0 && point.getLatitude() == 0);
    }
}
