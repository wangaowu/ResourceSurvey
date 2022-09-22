package com.bytemiracle.resourcesurvey.giscommon.location;

import android.location.LocationManager;

import org.osmdroid.util.GeoPoint;


/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/3/20 9:02
 */
public interface ILocation<T> {
    String TAG = "ILocation";
    int LOCATION_INTERVAL = 30 * 1000;

    /**
     * 开始定位
     *
     * @param interval                    定位时间
     * @param locationManager             定位对象
     * @param locationDataChangedListener 定位监听
     */
    void startLocate(long interval, LocationManager locationManager, LocationDataChangedListener locationDataChangedListener);

    /**
     * 停止定位
     */
    void stopLocate();

    /**
     * 重设定位时间
     *
     * @param interval 定位时间
     */
    void resetInterval(long interval);

    /**
     * 获取管理器
     *
     * @return 管理器
     */
    T getLocationManagerImpl();

    /**
     * 获取最后一次定位位置
     *
     * @return
     */
    GeoPoint getLastLocation();
}
