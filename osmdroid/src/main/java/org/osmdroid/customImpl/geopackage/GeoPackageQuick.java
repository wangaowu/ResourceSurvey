package org.osmdroid.customImpl.geopackage;

import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.geojson.Feature;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/2/26 14:44
 */
public class GeoPackageQuick {
    private static final String TAG = "GeoPackageQuick";

    //geopackage文件后缀
    public static final String GEOPACKAGE_SUFFIX = ".gpkg";
    //陕西省的经纬度边界
    //105°29′—111°15′，北纬31°42′—39°35′
    public static BoundingBox BOX_SHAANXI = new BoundingBox(105.29, 31.42, 111.15, 39.35);

    //默认的主键
    public static final String COL_PRIMARY_KEY = "fid";

    //默认的图形列
    public static final String COL_GEOMETRY = "geom";

    /**
     * 快速修正边界
     *
     * @param feature                    当前操作过的feature
     * @param geoPackage                 geopackage文件
     * @param updateBoundsResultListener 修正结果
     */
    public static void fixBounds(Feature feature, String tableName, mil.nga.geopackage.GeoPackage geoPackage, CommonAsyncListener<Integer> updateBoundsResultListener) {
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            long rowResult = -1;
            try {
                EditGeoPackage_ edit = new EditGeoPackage_(geoPackage);
                ReadGeoPackage_ read = getRead(geoPackage);
                String primaryKey = read.queryPrimaryKey(tableName);
                FeatureRow featureRow = read.queryRow(tableName, (Long) feature.getProperties().get(primaryKey));
                rowResult = edit.updateExtent(tableName, featureRow.getGeometry().getOrBuildEnvelope());
            } catch (Exception e) {
                Log.e(TAG, "fixBounds: " + e.getMessage());
            } finally {
                sink2Database(geoPackage);
            }
            final long r = rowResult;
            GlobalInstanceHolder.mainHandler().post(() ->
                    updateBoundsResultListener.doSomething(new Long(r).intValue()));
        });
    }

    /**
     * 获取geometry的边界
     *
     * @param geometryEnvelop
     * @return
     */
    public static BoundingBox fromEnvelop(GeometryEnvelope geometryEnvelop) {
        BoundingBox boundingBox = new BoundingBox();
        boundingBox.setMaxLatitude(geometryEnvelop.getMaxY());
        boundingBox.setMaxLongitude(geometryEnvelop.getMaxX());
        boundingBox.setMinLatitude(geometryEnvelop.getMinY());
        boundingBox.setMinLongitude(geometryEnvelop.getMinX());
        return boundingBox;
    }

    /**
     * 打开gpkg文件
     *
     * @param gpkgFile
     * @return
     */
    public static mil.nga.geopackage.GeoPackage connectExternalGeoPackage(String gpkgFile) {
        // Get a manager
        GeoPackageManager manager = GeoPackageFactory.getManager(GlobalInstanceHolder.applicationContext());
        // open external database
        return manager.openExternal(gpkgFile);
    }

    /**
     * 关闭连接
     *
     * @param geoPackage
     */
    public static void disconnectExternalGeoPackage(mil.nga.geopackage.GeoPackage geoPackage) {
        geoPackage.close();
    }

    /**
     * 确认执行所有任务
     *
     * @param geoPackage
     */
    public static void sink2Database(mil.nga.geopackage.GeoPackage geoPackage) {
        if (geoPackage.inTransaction()) {
            geoPackage.endTransaction();
        }
    }

    /**
     * 获取读取器
     *
     * @param geoPackage
     * @return
     */
    public static ReadGeoPackage_ getRead(mil.nga.geopackage.GeoPackage geoPackage) {
        return new ReadGeoPackage_(geoPackage);
    }

}
