package com.bytemiracle.resourcesurvey.modules.trajectory.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.resourcesurvey.giscommon.location.LocationDataChangedListener;

import java.lang.ref.WeakReference;

/**
 * 类功能：更新模块的数据持有类
 *
 * @author gwwang
 * @date 2021/3/11 16:25
 */
public class TrajectoryComponent {
    /**
     * 更新服务是否正在运行
     */
    public static boolean locationServiceIsRunning = false;
    private static LocationServiceConnection locationServiceConnection;
    private static Intent intent;

    private static boolean isBind = false;

    public static void setTrajectoryRunning(boolean isUpdating) {
        locationServiceIsRunning = isUpdating;
    }

    /**
     * 开启定位前台服务
     *
     * @param activity
     * @param locationInterval            定位间隔
     * @param locationMinMoveDistance     移动最小距离
     * @param locationDataChangedListener
     */
    public static void startService(BaseActivity activity, long locationInterval, double locationMinMoveDistance, LocationDataChangedListener locationDataChangedListener) {
        if (locationServiceIsRunning) {
            return;
        }
        isBind = false;
        intent = new Intent(activity, TrajectoryService.class);
        intent.setPackage(GlobalInstanceHolder.applicationContext().getPackageName());
        locationServiceConnection = new LocationServiceConnection(activity, locationInterval, locationMinMoveDistance, locationDataChangedListener);
        activity.startService(intent);
        isBind = activity.bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 停止定位前台服务
     *
     * @param activity
     */
    public static void stopService(BaseActivity activity) {
        if (locationServiceIsRunning && locationServiceConnection != null) {
            if (isBind) {
                activity.unbindService(locationServiceConnection);
                isBind = false;
            }
        }
        activity.stopService(intent);
        TrajectoryComponent.setTrajectoryRunning(false);
    }

    static class LocationServiceConnection implements ServiceConnection {
        private WeakReference<BaseActivity> activity;
        private long locationInterval;
        private double locationMinMoveDistance;
        LocationDataChangedListener locationDataChangedListener;

        public LocationServiceConnection(BaseActivity activity, long locationInterval, double locationMinMoveDistance, LocationDataChangedListener locationDataChangedListener) {
            this.activity = new WeakReference(activity);
            this.locationInterval = locationInterval;
            this.locationMinMoveDistance = locationMinMoveDistance;
            this.locationDataChangedListener = locationDataChangedListener;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            TrajectoryComponent.setTrajectoryRunning(false);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            TrajectoryComponent.setTrajectoryRunning(true);
            ((TrajectoryService.TrajectoryBinder) iBinder).startLocate(activity.get(), locationInterval, locationMinMoveDistance, locationDataChangedListener);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            TrajectoryComponent.setTrajectoryRunning(false);
        }
    }
}
