package com.bytemiracle.resourcesurvey.giscommon.location;

import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Pair;

import com.bytemiracle.base.framework.utils.sp.EasySharedPreference;
import com.bytemiracle.resourcesurvey.giscommon.location.utils.LocalCoordSysConvertUtils;
import com.bytemiracle.resourcesurvey.giscommon.location.utils.LocationConstant;
import com.bytemiracle.resourcesurvey.giscommon.location.utils.NavigationUtils;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;

import org.osmdroid.util.GeoPoint;

/**
 * 类功能：腾讯的定位
 *
 * @author gwwang
 * @date 2021/3/20 9:02
 */
public class TencentLocationImpl implements ILocation<TencentLocationManager> {

    private final TencentLocationManager mLocationManager;
    private TencentLocationRequest request;
    private TencentLocationListenerImpl tencentLocationListener;

    public TencentLocationImpl(Context applicationContext) {
        this.mLocationManager = TencentLocationManager.getInstance(applicationContext);
        mLocationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
    }

    public static void putAddressInfo(String address) {
        EasySharedPreference.get().putString(LocationConstant.KEY_SP.ADDRESS_INFO, address);
    }

    public static String getAddressInfo() {
        return EasySharedPreference.get().getString(LocationConstant.KEY_SP.ADDRESS_INFO, "");
    }

    @Override
    public void startLocate(long interval, LocationManager locationManager, LocationDataChangedListener locationDataChangedListener) {
        request = TencentLocationRequest.create();
        request.setInterval(interval);
        //设置请求级别(开启地理位置geo功能)
        request.setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_NAME);
        //是否允许使用GPS
        request.setAllowGPS(true);
        //是否需要获取传感器方向
        request.setAllowDirection(true);
        //是否需要开启室内定位
        request.setIndoorLocationMode(true);
        //开启定位
        mLocationManager.requestLocationUpdates(request, tencentLocationListener = new TencentLocationListenerImpl(locationDataChangedListener));
        //默认回调最后一次定位
        TencentLocation lastKnownLocation = mLocationManager.getLastKnownLocation();
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            double direction = lastKnownLocation.getDirection();
            locationDataChangedListener.onChanged(new GeoPoint(longitude, latitude), direction);
        }
    }

    @Override
    public void stopLocate() {
        if (tencentLocationListener != null) {
            mLocationManager.removeUpdates(tencentLocationListener);
        }
    }

    @Override
    public void resetInterval(long interval) {
        if (request != null) {
            request.setInterval(interval);
        }
    }

    @Override
    public TencentLocationManager getLocationManagerImpl() {
        return mLocationManager;
    }

    @Override
    public GeoPoint getLastLocation() {
        TencentLocation lastKnownLocation = mLocationManager.getLastKnownLocation();
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            TencentLocationImpl.putAddressInfo(lastKnownLocation.getAddress());
            return new GeoPoint(longitude, latitude);
        }
        return LocationConstant.DEFAULT_LOCATION;
    }

    class TencentLocationListenerImpl implements TencentLocationListener {
        private LocationDataChangedListener locationDataChangedListener;

        private GeoPoint preGcj2000Point = new GeoPoint(0d, 0d);

        public TencentLocationListenerImpl(LocationDataChangedListener locationDataChangedListener) {
            this.locationDataChangedListener = locationDataChangedListener;
        }

        @Override
        public void onLocationChanged(com.tencent.map.geolocation.TencentLocation tencentLocation, int i, String s) {
            if (tencentLocation != null) {
                //1.存放位置
                if (TextUtils.isEmpty(TencentLocationImpl.getAddressInfo())) {
                    TencentLocationImpl.putAddressInfo(tencentLocation.getAddress());
                }
                Pair<Double, Double> gcj2000Pair = LocalCoordSysConvertUtils.GCJ02ToGPS84(tencentLocation.getLongitude(), tencentLocation.getLatitude());
                GeoPoint gcj2000Point = new GeoPoint(gcj2000Pair.second, gcj2000Pair.first, tencentLocation.getAltitude());
                //2.每次响应基础回调
                locationDataChangedListener.onChanged(gcj2000Point, tencentLocation.getDirection());

                //proceed默认回调第一次定位(不移动)
                if (preGcj2000Point == null || (preGcj2000Point.getLongitude() == 0 && preGcj2000Point.getLatitude() == 0)) {
                    preGcj2000Point = gcj2000Point;
                    locationDataChangedListener.onProceedChanged(preGcj2000Point, gcj2000Point, tencentLocation.getAccuracy(), tencentLocation.getDirection());
                    return;
                }
                //3.proceed移动超过了最小距离,响应超过距离的回调
                double moveDistance = NavigationUtils.getDistance(preGcj2000Point, gcj2000Point);
                if (moveDistance >= locationDataChangedListener.initReflectMinimumDistance()) {
                    locationDataChangedListener.onProceedChanged(preGcj2000Point, gcj2000Point, tencentLocation.getAccuracy(), tencentLocation.getDirection());
                    preGcj2000Point = gcj2000Point;
                }
            }
        }

        @Override
        public void onStatusUpdate(String s, int i, String s1) {
        }
    }
}
