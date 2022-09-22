package org.osmdroid.customImpl.convert;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.gdal.ogr.DataSource;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.ogr;
import org.osmdroid.customImpl.convert.transform.From84GeometryWktImpl;
import org.osmdroid.customImpl.convert.transform.inter.GeometryDataPolicy;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.customImpl.geopackage.load.FeatureTableResultDTO;
import org.osmdroid.customImpl.geopackage.load.LoadGeopackageImpl;
import org.osmdroid.customImpl.geopackage.load.TableResultListener;
import org.osmdroid.customImpl.shp.GDALUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.GeometryType;

/**
 * 类功能：geopackage文件导出到shp文件中的实现
 *
 * @author gwwang
 * @date 2021/7/6 9:04
 */
public class GeoPackage2ShpImpl {
    private static final String TAG = "GeoPackage2ShpImpl";

    private String geoPackageFile;
    private GeometryDataPolicy geometryDataPolicy;
    private String shpParentDir;
    private CommonAsyncListener<String> getExportInfoListener;

    //别名存放数据容器
    //因shp不支持10个以上的字母字段名，3个以上的中文字段名
    //<AlisName "geopackage表内的实际名称" , FieldName "shp表内的对应名称">
    Map<String, String> fieldAliasCache = new HashMap<>();


    public GeoPackage2ShpImpl(String geoPackageFile) {
        this(geoPackageFile, new From84GeometryWktImpl());
    }

    public GeoPackage2ShpImpl(String geoPackageFile, GeometryDataPolicy geometryDataPolicy) {
        this.geoPackageFile = geoPackageFile;
        this.geometryDataPolicy = geometryDataPolicy;
    }

    /**
     * 将指定的geopackage导出到shp文件
     *
     * @param shpParentDir
     */
    public void execute(String shpParentDir, String prjWkt, CommonAsyncListener<String> getExportInfoListener) {
        this.shpParentDir = shpParentDir;
        this.getExportInfoListener = getExportInfoListener;
        //子线程运行
        Application context = GlobalInstanceHolder.applicationContext();
        LoadGeopackageImpl loadGeopackage = new LoadGeopackageImpl(context, new File(geoPackageFile));
        List<String> featureDaoNames = loadGeopackage.loadFeatureDaos().stream()
                .map(featureDao -> featureDao.getTableName())
                .collect(Collectors.toList());
        //没有可用的feature表
        if (ListUtils.isEmpty(featureDaoNames)) {
            getExportInfoListener.doSomething("没有可用数据!");
            return;
        }
        fieldAliasCache.clear();
        getExportInfoListener.doSomething("正在准备...");
        getExportInfoListener.doSomething("共" + featureDaoNames.size() + "表[ " + featureDaoNames.toString() + " ]导出中...");
        loadGeopackage.syncLoadAll(new TableResultListener() {
            @Override
            public void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult) {
                //2.加载所有数据完成之后，将所有数据拷贝到1创建好的表中
                if (tableResult == null || tableResult.isEmpty()) {
                    getExportInfoListener.doSomething("读取gpkg文件内表异常...");
                    getExportInfoListener.doSomething("导出终止!");
                    return;
                }
                for (Map.Entry<String, FeatureTableResultDTO> entry : tableResult.entrySet()) {
                    String tableName = entry.getKey();
                    List<FeatureRow> featureRows = entry.getValue().getFeatureRows();
                    Optional<FeatureDao> featureDao = loadGeopackage.loadFeatureDao(tableName);
                    if (!featureDao.isPresent()) {
                        getExportInfoListener.doSomething("读取gpkg文件内[ " + tableName + " ]异常!");
                        continue;
                    }
                    if (ListUtils.isEmpty(featureRows)) {
                        getExportInfoListener.doSomething("读取gpkg文件内[ " + tableName + " ]要素为空!");
                        continue;
                    }
                    //3.创建shp文件和表字段信息
                    String newShpFile = createShpFile(featureDao.get(), prjWkt);
                    if (TextUtils.isEmpty(newShpFile)) {
                        getExportInfoListener.doSomething("创建shp文件失败!");
                        continue;
                    }
                    sinkFeatures2Shp(featureRows, new File(newShpFile), prjWkt);
                    getExportInfoListener.doSomething("表[" + tableName + "]中数据拷贝完成,共" + featureRows.size() + "条");
                }
                getExportInfoListener.doSomething("全部数据导出完成,共" + tableResult.size() + "张表");
                getExportInfoListener.doSomething("存放路径目录: " + shpParentDir);
            }
        });
    }

    //拷贝feature表内所有的行数据
    private void sinkFeatures2Shp(List<FeatureRow> allFeatures, File shpFile, String prjWkt) {
        //3.3.1拷贝单行数据
        DataSource dateSource = GDALUtils.getDateSource(shpFile.getAbsolutePath());
        for (FeatureRow featureRow : allFeatures) {
            Log.d(TAG, "准备增加行 : " + featureRow.getId());
            int fid = GDALUtils.addNewRowFromGPKG(dateSource, null, featureRow, fieldAliasCache, geometryDataPolicy, prjWkt);
            if (fid < 0) {
                Log.d(TAG, "增加行失败！");
            } else {
                Log.d(TAG, "增加行成功！");
            }
        }
        //整张表拷贝完成之后，修改extent
        GDALUtils.fixExtent(shpFile.getAbsolutePath());
    }

    //创建shp文件
    private String createShpFile(FeatureDao featureDao, String prjWkt) {
        String tableName = featureDao.getTableName();
        String shpFile = shpParentDir + File.separator + tableName + ".shp";
        int ogrGeometryType = getOgrGeometryType(featureDao.getGeometryType());
        List<FieldDefn> ogrFields = getOgrFields(featureDao.getColumns());
        boolean createSuccess = GDALUtils.createNewShpFile(shpFile, tableName, prjWkt, ogrGeometryType, ogrFields);
        return createSuccess ? shpFile : null;
    }

    //转成gdal的字段定义
    private List<FieldDefn> getOgrFields(List<FeatureColumn> featureColumns) {
        getExportInfoListener.doSomething("字段映射信息如下:");
        List<FieldDefn> fieldDefns = new ArrayList<>();
        for (FeatureColumn field : featureColumns) {
            String aliasName = field.getName();
            if (!GeoPackageQuick.COL_GEOMETRY.equalsIgnoreCase(aliasName) && !GeoPackageQuick.COL_PRIMARY_KEY.equalsIgnoreCase(aliasName)) {
                String fieldName = putFieldAliasCache(aliasName); //避免shp不支持的字段名称，英文10个，中文3个
                FieldDefn fieldDefn = new FieldDefn(fieldName, getOgrFieldType(field.getDataType()));
                fieldDefns.add(fieldDefn);
                getExportInfoListener.doSomething("\"" + aliasName + "\"" + "----->" + "\"" + fieldName + "\"");
            }
        }
        return fieldDefns;
    }

    private String putFieldAliasCache(String aliasName) {
        String fieldName = aliasName;
        if (isContainChinese(fieldName)) {
            //shp字段含有中文，取后三个
            if (fieldName.length() > 3) {
                fieldName = fieldName.substring(fieldName.length() - 3);
            }
        } else {
            //shp字段英文，取后10个
            if (fieldName.length() > 10) {
                fieldName = fieldName.substring(fieldName.length() - 10);
            }
        }
        fieldAliasCache.put(aliasName, fieldName);
        return fieldName;
    }

    /**
     * 判断字符串中是否包含中文
     */
    private boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    //将geoFeature字段类型转换为gdal的类型
    private int getOgrFieldType(GeoPackageDataType fieldType) {
        switch (fieldType) {
            case REAL:
            case FLOAT:
            case DOUBLE:
                return ogr.OFTReal;
            case TINYINT:
            case MEDIUMINT:
            case SMALLINT:
                return ogr.OFTInteger;
            case INT:
            case INTEGER:
                return ogr.OFTInteger64;
            case DATE:
                return ogr.OFTDate;
            case DATETIME:
                return ogr.OFTDateTime;
            case BOOLEAN:
                return ogr.OFSTBoolean;
            case TEXT:
            default:
                return ogr.OFTString;
        }
    }

    //转成gdal的图形类型
    private int getOgrGeometryType(GeometryType geometryType) {
        switch (geometryType) {
            case MULTIPOINT:
                return ogr.wkbMultiPoint;
            case POINT:
                return ogr.wkbPoint;
            case MULTILINESTRING:
            case LINESTRING:
                return ogr.wkbMultiLineString;
            case MULTIPOLYGON:
            case POLYGON:
            default:
                return ogr.wkbMultiPolygon;
        }
    }
}
