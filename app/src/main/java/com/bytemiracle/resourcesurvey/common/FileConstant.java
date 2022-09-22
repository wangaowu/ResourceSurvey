package com.bytemiracle.resourcesurvey.common;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * 类功能：文件相关常量
 *
 * @author gwwang
 * @date 2021/3/16 14:32
 */
public class FileConstant {
    private static final String APK_PATH_NAME = "apk";

    private static final String ROOT_DIR_KEY_WORD = "RSurvey";

    public static final String[] LIST_IN_ROOT = {"databases", "工程列表", "默认模板"};

    public static final String[] LIST_IN_PROJECT = {"采集数据", "多媒体", "轨迹文件", "数据导出", "ERROR"};

    public static final String[] LIST_IN_MEDIA = {"草图", "截图", "视频", "录音", "图片", "绘图"};

    public static String getAppDataDir(Context context) {
        return context.getExternalCacheDir().getAbsolutePath();
    }

    public static String getApkPath(Context context) {
        return getAppDataDir(context) + File.separator + APK_PATH_NAME;
    }

    /**
     * 配置数据库路径
     *
     * @return
     */
    public static String getDatabaseFile() {
        return getExternalRootDir() + File.separator + "databases" + File.separator + "rsurvey.db";
    }

    /**
     * 确保文件夹存在
     *
     * @param path
     * @return
     */
    public static String ensureDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    /**
     * 获取配置该应用的根路径
     *
     * @return
     */
    public static String getExternalRootDir() {
        String rootDir = Environment.getExternalStorageDirectory() + File.separator + ROOT_DIR_KEY_WORD;
        ensureDir(rootDir);
        //创建根路径下的的默认文件夹
        for (String dir : LIST_IN_ROOT) {
            ensureDir(rootDir + File.separator + dir);
        }
        return rootDir;
    }
}
