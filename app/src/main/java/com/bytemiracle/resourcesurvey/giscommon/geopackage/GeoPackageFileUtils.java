package com.bytemiracle.resourcesurvey.giscommon.geopackage;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;

import org.osmdroid.customImpl.geopackage.GeoPackageQuick;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/2/26 14:43
 */
public class GeoPackageFileUtils {
    /**
     * 拷贝geoPackage文件
     *
     * @param destGeoPackage
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void asyncCopyGeoPackageFile(String geoPackageFilePath, String destGeoPackage, CommonAsyncListener<Boolean> listener) {
        File geoPackageFile = new File(geoPackageFilePath);
        if (!geoPackageFile.exists()) {
            // 文件不存在
            listener.doSomething(false);
            return;
        }
        //子线程拷贝geoPackage文件文件到当前工程下
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            com.xuexiang.xutil.file.FileUtils.copyFile(geoPackageFilePath, destGeoPackage, null);
            GlobalInstanceHolder.mainHandler().post(() -> listener.doSomething(true));
        });
    }

    /**
     * 删除geoPackage文件
     *
     * @param geoPackagePath
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void deleteGeoPackageOfProject(String geoPackagePath, CommonAsyncListener<Boolean> listener) {
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            boolean deleteSuccess = new File(geoPackagePath).delete();
            GlobalInstanceHolder.mainHandler().post(() -> listener.doSomething(deleteSuccess));
        });
    }
}
