package org.osmdroid.config;

import android.os.Environment;

import java.io.File;

/**
 * 类功能：osmdroid的配置类
 *
 * @author gwwang
 * @date 2022/1/26 13:32
 */
public class OsmSDKConfig {

    private static final String OSMDROID_CACHE = Environment.getExternalStorageDirectory().getAbsolutePath() + "/osmdroidCache";

    static {
        setDebuggable(false);
    }

    /**
     * 设置是否调试模式
     *
     * @param debuggable
     */
    public static void setDebuggable(boolean debuggable) {
        Configuration.getInstance().setDebugMode(debuggable);
        Configuration.getInstance().setDebugMapView(debuggable);
        Configuration.getInstance().setDebugMapTileDownloader(debuggable);
    }

    /**
     * 设置工作路径(缓存路径)
     * 缓存形式 :
     * 1.SDK_API>10: SQLITE
     * 2.SDK_API<=0: imageFile
     */
    public static void setCachePath(String path) {
        File cacheFile = new File(path);
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }
        Configuration.getInstance().setOsmdroidBasePath(cacheFile);
    }

}
