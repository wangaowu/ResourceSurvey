package org.osmdroid.customImpl.geopackage.load;

import android.content.Context;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.overlay.bean.PackageOverlayInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageFactory;
import mil.nga.geopackage.GeoPackageManager;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：基础加载方式
 *
 * @author gwwang
 * @date 2022/2/23 15:57
 */
public class BaseGeopackageLoad {
    private static final String TAG = "BaseGeopackageLoad";

    protected final GeoPackageManager manager;
    protected File gpkgFile;
    protected GeoPackage geoPackage;

    /**
     * 构造方法
     *
     * @param context  上下文
     * @param gpkgFile gpkg文件
     */
    public BaseGeopackageLoad(Context context, File gpkgFile) {
        this.gpkgFile = gpkgFile;
        this.manager = GeoPackageFactory.getManager(context);
        this.geoPackage = GeoPackageQuick.connectExternalGeoPackage(gpkgFile.getAbsolutePath());
        loadPrepare();
    }

    public GeoPackage getGeoPackage() {
        return geoPackage;
    }

    /**
     * 加载数据库中所有的feature表信息
     *
     * @return 所有的feature表
     */
    public List<FeatureDao> loadFeatureDaos() {
        List<FeatureDao> featureDaos = new ArrayList<>();
        List<String> featureTables = geoPackage.getFeatureTables();
        for (String featureTable : featureTables) {
            FeatureDao featureDao = geoPackage.getFeatureDao(featureTable);
            featureDaos.add(featureDao);
        }
        return featureDaos;
    }

    /**
     * 获取某张表
     *
     * @param featureTableName
     * @return
     */
    public Optional<FeatureDao> loadFeatureDao(String featureTableName) {
        return loadFeatureDaos().stream()
                .filter(featureDao -> featureDao.getTableName().equals(featureTableName))
                .findFirst();
    }

    /**
     * 加载所有的featureTable
     *
     * @param tableResultListener
     */
    public void syncLoadAll(TableResultListener tableResultListener) {
        List<FeatureDao> featureDaos = loadFeatureDaos();
        if (ListUtils.isEmpty(featureDaos)) {
            tableResultListener.onReadTableResult(null);
            return;
        }
        Map<String, FeatureTableResultDTO> allTableResult = new HashMap();
        for (FeatureDao featureDao : featureDaos) {
            syncLoad(featureDao, null, null, new TableResultListener() {
                @Override
                public void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult) {
                    allTableResult.putAll(tableResult);
                    if (allTableResult.size() == featureDaos.size()) {
                        //所有表读取完成
                        tableResultListener.onReadTableResult(allTableResult);
                    }
                }
            });
        }
    }

    /**
     * 执行加载 (please使用异步)
     *
     * @param featureTable        feature表名
     * @param sql                 sql语句
     * @param selectionArgs       占位符参数
     * @param TableResultListener 结果回调
     */
    public void syncLoad(String featureTable,
                         String sql, String[] selectionArgs,
                         TableResultListener TableResultListener) {
        Optional<FeatureDao> featureDao1 = loadFeatureDao(featureTable);
        if (!featureDao1.isPresent()) {
            Map<String, FeatureTableResultDTO> tableResult = new HashMap<>();
            tableResult.put(featureTable, new FeatureTableResultDTO(PackageOverlayInfo.OSMGeometryType.POLYGON, new ArrayList<>()));
            TableResultListener.onReadTableResult(tableResult);
            return;
        }
        FeatureDao featureDao = featureDao1.get();
        syncLoad(featureDao, sql, selectionArgs, TableResultListener);
    }

    /**
     * 执行加载 (please使用异步)
     *
     * @param featureDao          feature表
     * @param sql                 sql语句
     * @param selectionArgs       占位符参数
     * @param tableResultListener 结果回调
     */
    public void syncLoad(FeatureDao featureDao,
                         String sql, String[] selectionArgs,
                         TableResultListener tableResultListener) {
        FeatureCursor featureCursor;
        if (TextUtils.isEmpty(sql)) {
            featureCursor = featureDao.queryForAll();
        } else {
            String querySQL = "select * from " + featureDao.getTableName() + " where " + sql;
            featureCursor = featureDao.rawQuery(querySQL, selectionArgs);
        }
        PackageOverlayInfo.OSMGeometryType geometryType = FeatureTableResultDTO.Companion.matchOSMGeometryType(featureDao.getGeometryType());
        Map<String, FeatureTableResultDTO> tableResult = new HashMap<>();
        List<FeatureRow> featureRows = new ArrayList<>();
        try {
            while (featureCursor.moveToNext()) {
                featureRows.add(featureCursor.getRow());
            }
            tableResult.put(featureDao.getTableName(), new FeatureTableResultDTO(geometryType, featureRows));
            tableResultListener.onReadTableResult(tableResult);
        } catch (Exception e) {
            tableResult.put(featureDao.getTableName(), new FeatureTableResultDTO(geometryType, featureRows));
            tableResultListener.onReadTableResult(tableResult);
            e.printStackTrace();
        } finally {
            featureCursor.close();
        }
    }

    /**
     * 获取字段名称
     */
    public Map<String, List<String>> syncGetFieldNames() {
        Map<String, List<String>> map = new ArrayMap<>();
        List<FeatureDao> featureDaos = loadFeatureDaos();
        for (FeatureDao featureDao : featureDaos) {
            String tableName = featureDao.getTableName();
            map.put(tableName, syncGetFieldNames(tableName));
        }
        return map;
    }

    /**
     * 获取表内字段名称
     *
     * @param tableName
     */
    public List<String> syncGetFieldNames(String tableName) {
        List<String> fieldsName = new ArrayList<>();
        Optional<FeatureDao> featureDao1 = loadFeatureDao(tableName);
        if (featureDao1.isPresent()) {
            List<FeatureColumn> columns = featureDao1.get().getTable().getColumns();
            for (FeatureColumn column : columns) {
                if (!column.isPrimaryKey() &&
                        !column.isGeometry()) {
                    fieldsName.add(column.getName());
                }
            }
        }
        return fieldsName;
    }

    /**
     * 是否含有该表信息
     *
     * @param geoPackage
     * @param tableName
     * @return
     */
    public boolean hasTable(GeoPackage geoPackage, String tableName) {
        return geoPackage.getFeatureTables().stream()
                .anyMatch(s -> tableName.equals(s));
    }

    private void loadPrepare() {
        Log.d(TAG, "databaseName=" + geoPackage.getName() + ",尝试导入...");
        try {
            if (!manager.importGeoPackage(gpkgFile, true)) {
                Log.e(TAG, "databaseName=" + geoPackage.getName() + "导入失败!");
            }
        } catch (Exception ex) {
            Log.e(TAG, "databaseName=" + geoPackage.getName() + "导入失败,:" + ex.toString());
        }
    }
}
