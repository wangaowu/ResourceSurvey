package com.bytemiracle.resourcesurvey.giscommon.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.XToastUtils;

import org.osmdroid.util.GeoPoint;

/**
 * 类功能：应用定位
 *
 * @author gwwang
 * @date 2021/3/20 9:09
 */
public class AppLocationManager {
    private static final String TAG = "AppLocationManager";

    private static AppLocationManager instance;

    public static AppLocationManager getInstance(Context applicationContext) {
        if (instance == null) {
            LocationManager locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
            instance = new AppLocationManager(locationManager, new TencentLocationImpl(applicationContext));
            //instance = new AppLocationManager(locationManager, new SuperMapLocationImpl());
        }
        return instance;
    }

    /**
     * 判断定位服务是否开启
     *
     * @param
     * @return true 表示开启
     */
    public boolean isLocationEnabled() {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(GlobalInstanceHolder.applicationContext().getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(GlobalInstanceHolder.applicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private LocationManager locationManager;
    private ILocation<TencentLocationImpl> ilocation;

    private AppLocationManager(LocationManager locationManager, ILocation ilocation) {
        this.locationManager = locationManager;
        this.ilocation = ilocation;
    }

    public void startLocate(Activity activity, long interval, LocationDataChangedListener locationDataChangedListener) {
        if (!isLocationEnabled()) {
            XToastUtils.info("应用请求位置失败，请打开位置信息!");
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }
        ilocation.startLocate(interval, locationManager, locationDataChangedListener);
    }

    public void getLocationData() {
        TencentLocationImpl tencentLocationImpl = ilocation.getLocationManagerImpl();
    }

    public void stopLocate() {
        ilocation.stopLocate();
    }

    public GeoPoint getLastLocation() {
        return ilocation.getLastLocation();
    }
}
