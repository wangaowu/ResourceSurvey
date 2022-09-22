package org.osmdroid.cache;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;

import org.osmdroid.defaultImpl.R;
import org.osmdroid.tileprovider.cachemanager.CacheManager;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;

/**
 * 类功能：瓦片缓存服务
 *
 * @author gwwang
 * @date 2021/3/10 11:32
 */
public class CacheTilesService extends Service {
    private static final String TAG = "CacheTilesService";
    private static final int NOTIFY_ID = 1002;
    private static final String CHANNEL_ID = "channel_cache_tiles";
    private static final CharSequence CHANNEL_NAME = "channel_cache_tiles";

    private CacheBinder cacheBinder;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        CacheTilesComponent.setServiceRunning(true);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification(0, 1);
        startForeground(NOTIFY_ID, notification);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "瓦片下载服务开始: " + "onBind() called");
        return cacheBinder = new CacheBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopCacheTasks();
        stopForeground(true);
        return super.onUnbind(intent);
    }

    private void stopCacheTasks() {
        if (cacheBinder != null && cacheBinder.cacheManager != null) {
            cacheBinder.cacheManager.cancelAllJobs();
            Log.e(TAG, "瓦片下载服务停止: release() called");
        }
        CacheTilesComponent.setServiceRunning(false);
    }

    /**
     * 初始化通知
     *
     * @param progress 当前进度
     * @param max      最大
     */
    private Notification createNotification(int progress, int max) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.enableLights(false);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("缓存瓦片中")
                .setSmallIcon(R.drawable.ic_cache_tiles)
                .setContentText("进度:" + progress + "/" + max)
                .setProgress(progress, max, true)
                .setOngoing(true);
        return mBuilder.build();
    }

    private void updateNotificationProgress(int progress, int max) {
        Notification notification = createNotification(progress, max);
        notificationManager.notify(NOTIFY_ID, notification);
    }

    /**
     * 轨迹服务中间Binder对象
     */
    public class CacheBinder extends Binder {

        public CacheManager cacheManager;

        public void startCache(Context applicationCtx, ITileSource tileSource, BoundingBox cacheBox, int zoomMin, int zoomMax, CommonAsyncListener<Pair<Integer, Integer>> cacheProgressListener) {
            cacheManager = new CacheManager(tileSource, new SqlTileWriter(), zoomMin, zoomMax);
            cacheManager.downloadAreaAsync(applicationCtx, cacheBox, zoomMin, zoomMax, new CacheManager.CacheManagerCallback() {

                private int total;

                @Override
                public void onTaskComplete() {
                    CacheTilesComponent.stopService(applicationCtx);
                    XToastUtils.info("地图瓦片全部缓存完成!");
                }

                @Override
                public void updateProgress(int progress, int currentZoomLevel, int zoomMin, int zoomMax) {
                    updateNotificationProgress(progress, total);
                    cacheProgressListener.doSomething(new Pair(progress, total));
                }

                @Override
                public void downloadStarted() {
                    cacheProgressListener.doSomething(new Pair(0, total));
                }

                @Override
                public void setPossibleTilesInArea(int total) {
                    this.total = total;
                }

                @Override
                public void onTaskFailed(int errors) {
                    XToastUtils.info("地图瓦片缓存失败: " + errors);
                }
            });
        }
    }
}
