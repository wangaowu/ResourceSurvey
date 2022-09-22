package com.bytemiracle.resourcesurvey.modules.trajectory.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.giscommon.location.AppLocationManager;
import com.bytemiracle.resourcesurvey.giscommon.location.LocationDataChangedListener;

import org.osmdroid.util.GeoPoint;

/**
 * 类功能：打点服务
 *
 * @author gwwang
 * @date 2021/3/10 11:32
 */
public class TrajectoryService extends Service {
    private static final String TAG = "TrajectoryService";
    private static final int NOTIFY_ID = 1003;
    private static final String CHANNEL_ID = "channel_trajectory";
    private static final CharSequence CHANNEL_NAME = "channel_trajectory";

    private TrajectoryBinder trajectoryBinder;

    @Override
    public void onCreate() {
        TrajectoryComponent.setTrajectoryRunning(true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification();
        startForeground(NOTIFY_ID, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "定位服务开始: " + "onBind() called");
        return trajectoryBinder = new TrajectoryBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopBinderLocation();
        stopForeground(true);
        return super.onUnbind(intent);
    }

    private void stopBinderLocation() {
        if (trajectoryBinder != null && trajectoryBinder.locationManager != null) {
            trajectoryBinder.locationManager.stopLocate();
            Log.e(TAG, "定位服务停止: release() called");
        }
        TrajectoryComponent.setTrajectoryRunning(false);
    }

    /**
     * 初始化通知
     */
    private Notification createNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.enableLights(false);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("正在记录您的轨迹")
                .setSmallIcon(R.drawable.ic_launcher)
                .setOngoing(true)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());
        return mBuilder.build();
    }

    /**
     * 轨迹服务中间Binder对象
     */
    public class TrajectoryBinder extends Binder {

        public AppLocationManager locationManager;

        public void startLocate(BaseActivity activity, long locationInterval, double locationMinMoveDistance, LocationDataChangedListener locationDataChangedListener) {
            locationManager = AppLocationManager.getInstance(GlobalInstanceHolder.applicationContext());
            locationManager.startLocate(activity, locationInterval, new LocationDataChangedListener() {
                @Override
                public void onProceedChanged(GeoPoint oldPoint2D, GeoPoint newPoint2D, double accuracy, double v) {
                    if (!activity.isFinishing()) {
                        locationDataChangedListener.onProceedChanged(oldPoint2D, newPoint2D, accuracy, v);
                    }
                }

                @Override
                public double initReflectMinimumDistance() {
                    return locationMinMoveDistance;
                }
            });
        }
    }
}
