package org.osmdroid.cache;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Pair;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BoundingBox;

/**
 * 类功能：瓦片缓存组件管理
 *
 * @author gwwang
 * @date 2021/3/11 16:25
 */
public class CacheTilesComponent {
    /**
     * 服务是否正在运行
     */
    public static boolean serviceIsRunning = false;
    private static CacheTilesServiceConnection cacheTilesServiceConnection;
    private static Intent intent;

    private static boolean isBind = false;

    public static void setServiceRunning(boolean isRunning) {
        serviceIsRunning = isRunning;
    }

    /**
     * 开启缓存服务
     *
     * @param applicationCtx
     * @param cacheProgressListener
     */
    public static void startService(Context applicationCtx, ITileSource tileSource, BoundingBox box, int zoomMin, int zoomMax, CommonAsyncListener<Pair<Integer, Integer>> cacheProgressListener) {
        if (serviceIsRunning) {
            return;
        }
        isBind = false;
        intent = new Intent(applicationCtx, CacheTilesService.class);
        intent.setPackage(applicationCtx.getPackageName());
        cacheTilesServiceConnection = new CacheTilesServiceConnection(applicationCtx, tileSource, box, zoomMin, zoomMax, cacheProgressListener);
        applicationCtx.startService(intent);
        isBind = applicationCtx.bindService(intent, cacheTilesServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 停止定位前台服务
     *
     * @param applicationCtx
     */
    public static void stopService(Context applicationCtx) {
        if (serviceIsRunning && cacheTilesServiceConnection != null) {
            if (isBind) {
                applicationCtx.unbindService(cacheTilesServiceConnection);
                isBind = false;
            }
        }
        applicationCtx.stopService(intent);
        CacheTilesComponent.setServiceRunning(false);
    }

    static class CacheTilesServiceConnection implements ServiceConnection {
        private Context applicationCtx;
        private ITileSource tileSource;
        private BoundingBox cacheBox;
        private int zoomMin;
        private int zoomMax;
        private CommonAsyncListener<Pair<Integer, Integer>> cacheProgressListener;

        public CacheTilesServiceConnection(Context applicationCtx, ITileSource tileSource, BoundingBox cacheBox, int zoomMin, int zoomMax, CommonAsyncListener<Pair<Integer, Integer>> cacheProgressListener) {
            this.applicationCtx = applicationCtx;
            this.tileSource = tileSource;
            this.cacheBox = cacheBox;
            this.zoomMin = zoomMin;
            this.zoomMax = zoomMax;
            this.cacheProgressListener = cacheProgressListener;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CacheTilesComponent.setServiceRunning(false);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            CacheTilesComponent.setServiceRunning(true);
            ((CacheTilesService.CacheBinder) iBinder).startCache(applicationCtx, tileSource, cacheBox, zoomMin, zoomMax, cacheProgressListener);
        }

        @Override
        public void onBindingDied(ComponentName name) {
            CacheTilesComponent.setServiceRunning(false);
        }
    }
}
