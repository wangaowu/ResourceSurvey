package org.osmdroid.customImpl.convert;

import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.xuexiang.xutil.file.FileUtils;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogrConstants;
import org.osmdroid.customImpl.convert.transform.SpatialReferenceCompact;
import org.osmdroid.customImpl.convert.transform.To84GeometryWktImpl;
import org.osmdroid.customImpl.convert.transform.inter.GeometryDataPolicy;
import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.FieldDefn;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.customImpl.geopackage.ReadGeoPackage_;
import org.osmdroid.customImpl.shp.LoadShpImpl;
import org.osmdroid.customImpl.shp.Shp_;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.sf.GeometryType;

/**
 * 类功能：shp文件加载到geopackage文件中的实现
 *
 * @author gwwang
 * @date 2021/7/6 8:57
 */
public class Shp2GeoPackageImpl {
    private static final String TAG = "Shp2GeoPackageImpl";

    private static final GeometryDataPolicy DEFAULT_GEOMETRY_DATA_POLICY = new To84GeometryWktImpl();

    private String shpFile;
    private String tableNameInGPKG;
    private String geoPackageFile;
    private GeometryDataPolicy geometryDataPolicy;

    /**
     * 构造方法
     *
     * @param shpFile        shp文件路径
     * @param geoPackageFile geopackage文件路径
     */
    public Shp2GeoPackageImpl(String shpFile, String geoPackageFile) {
        this(shpFile, geoPackageFile, DEFAULT_GEOMETRY_DATA_POLICY);
    }

    /**
     * 构造方法
     *
     * @param shpFile            shp文件路径
     * @param geoPackageFile     geopackage文件路径
     * @param geometryDataPolicy 图形数据转换策略
     */
    public Shp2GeoPackageImpl(String shpFile, String geoPackageFile, GeometryDataPolicy geometryDataPolicy) {
        this.shpFile = shpFile;
        this.tableNameInGPKG = FileUtils.getFileNameNoExtension(shpFile);
        this.geoPackageFile = geoPackageFile;
        this.geometryDataPolicy = (geometryDataPolicy != null) ? geometryDataPolicy : DEFAULT_GEOMETRY_DATA_POLICY;
    }

    /**
     * 将指定文件加载到geoPackage
     *
     * @param getGeoFeatureTableNameListener 插入成功后，geopackageFeatureTable的名称回调
     */
    public void execute(CommonAsyncListener<String> getGeoFeatureTableNameListener) {
        if (!shpFile.endsWith("shp") && !shpFile.endsWith("SHP")) {
            XToastUtils.info("不被支持的shp文件");
            GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
            return;
        }
        if (!Shp_.FileUtils.isAvailableShp(shpFile)) {
            XToastUtils.info("添加图层失败，请检查shp文件完整性");
            GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
            return;
        }
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            GeoPackage geoPackage = GeoPackageQuick.connectExternalGeoPackage(geoPackageFile);
            if (geoPackage.getFeatureTables().contains(tableNameInGPKG)) {
                GeoPackageQuick.sink2Database(geoPackage);
                GlobalInstanceHolder.mainHandler().post(() -> XToastUtils.info(tableNameInGPKG + "已存在,不能添加重复图层!"));
                GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
                return;
            }
            createGeoPackageFeatureTable(geoPackage, getGeoFeatureTableNameListener);
        });
    }

    private void createGeoPackageFeatureTable(GeoPackage geoPackage, CommonAsyncListener<String> getGeoFeatureTableNameListener) {
        try {
            //1.加载shp文件
            LoadShpImpl loadShpImpl = new LoadShpImpl(null, new File(shpFile));
            DataSource dataSource = loadShpImpl.getDataSource();
            if (dataSource == null) {
                String error = "Shapefile failed to load: shp文件内没有feature数据!";
                GlobalInstanceHolder.mainHandler().post(() -> XToastUtils.info(error));
                GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
                return;
            }
            //2.使用shp文件信息创建geopackage的表
            Layer defaultLayer = loadShpImpl.readLayer();
            FeatureDefn gdalFeatureDefn = defaultLayer.GetLayerDefn();
            int getGeomType = defaultLayer.GetGeomType();
            List<FieldDefn> geoFieldDefines = getSfFieldsFromShpLayer(gdalFeatureDefn, getGeomType);
            SpatialReferenceSystem WGS84spatialReferenceSystem = GeoPackageQuick.getRead(geoPackage)
                    .querySpatialReferenceSystem(4326);
            int rowCount = new EditGeoPackage_(geoPackage)
                    .addFeatureTableSpatialName(tableNameInGPKG, geoFieldDefines, WGS84spatialReferenceSystem);
            if (rowCount != 1) {
                GeoPackageQuick.sink2Database(geoPackage);
                GlobalInstanceHolder.mainHandler().post(() -> XToastUtils.info("创建featureTable of geoPackage失败!"));
                GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
                return;
            }
            //3.将shp文件的属性数据复制到geopackage对应的表中
            //3.1 加载上步生成的新表
            List<Feature> allFeatures = loadShpImpl.getAllFeatures(defaultLayer);
            //3.3查询成功之后，将所有数据拷贝到2创建好的表中
            copyAllFeaturesFromShp2(geoPackage, allFeatures, getGeoFeatureTableNameListener);
        } catch (Exception e) {
            Log.e(TAG, "createGeopackageFeatureTable: " + e);
            GlobalInstanceHolder.mainHandler().post(() -> XToastUtils.info("失败: " + e.getMessage()));
            GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(null));
        } finally {
            GeoPackageQuick.sink2Database(geoPackage);
        }
    }

    private void copyAllFeaturesFromShp2(GeoPackage geoPackage, List<Feature> shpFeatures, CommonAsyncListener<String> getGeoFeatureTableNameListener) throws Exception {
        if (ListUtils.isEmpty(shpFeatures)) {
            //4.1shp中没有有效的数据，仅创建表成功
            GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(tableNameInGPKG));
            return;
        }
        //4.2遍历拷贝每一行数据
        Log.e(TAG, "copyAllFeaturesFromShp 共: " + shpFeatures.size());
        EditGeoPackage_ edit = new EditGeoPackage_(geoPackage);
        int[] shouldCount = new int[]{0};
        for (Feature shpFeature : shpFeatures) {
            addSingleFeatureAsync(shpFeature, edit, success -> {
                shouldCount[0]++;
                Log.e(TAG, "copyAllFeaturesFromShp2: " + shouldCount[0]);
                if (shouldCount[0] == shpFeatures.size() - 1) {
                    GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(tableNameInGPKG));
                }
            });
        }
    }

    //异步递归是很确定，但数据量过大会造成 stackOverFlow crash
    private void copyAllFeaturesFromShp(GeoPackage geoPackage, List<Feature> shpFeatures, CommonAsyncListener<String> getGeoFeatureTableNameListener) {
        ReadGeoPackage_ read = GeoPackageQuick.getRead(geoPackage);
        EditGeoPackage_ edit = new EditGeoPackage_(geoPackage);
        Iterator<Feature> iterator = shpFeatures.iterator();
        if (!iterator.hasNext()) {
            //4.1shp中没有有效的数据，仅创建表成功
            GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(tableNameInGPKG));
            return;
        }
        //4.2遍历拷贝每一行数据
        addSingleFeatureAsync(iterator.next(), edit, new CommonAsyncListener<Boolean>() {
            @Override
            public void doSomething(Boolean addFeatureSuccess) {
                if (!iterator.hasNext()) {
                    //5.遍历拷贝所有数据完成
                    GlobalInstanceHolder.mainHandler().post(() -> getGeoFeatureTableNameListener.doSomething(tableNameInGPKG));
                    return;
                }
                addSingleFeatureAsync(iterator.next(), edit, this);
            }
        });
    }

    //添加geopackge对应表中的feature行数据
    private void addSingleFeatureAsync(Feature shpFeature, EditGeoPackage_ edit,
                                       CommonAsyncListener<Boolean> addFeatureResultListener) {
        Geometry shpGeometryRef = shpFeature.GetGeometryRef();
        if (shpGeometryRef == null || shpGeometryRef.IsEmpty()) {
            //4.3忽略shp中无效的geometry行
            addFeatureResultListener.doSomething(true);
            return;
        }
        try {
            //4.4 创建新行，设置属性图形，并添加
            FeatureRow featureRow = createNewRowWithShp(edit, shpFeature);
            edit.addFeature(tableNameInGPKG, featureRow);
            //4.6更新完成之后，刷一下extents
            edit.updateExtent(tableNameInGPKG, featureRow.getGeometry().getOrBuildEnvelope());
            addFeatureResultListener.doSomething(true);
        } catch (Exception e) {
            Log.e(TAG, "addFeature失败: " + e.getMessage());
            Log.e(TAG, "addFeature失败: " + gdal.GetLastErrorMsg());
            addFeatureResultListener.doSomething(false);
        }
    }

    private FeatureRow createNewRowWithShp(EditGeoPackage_ edit, Feature shpFeature) throws IOException {
        FeatureRow featureRow = edit.getFeatureDao(tableNameInGPKG).newRow();
        //给新行设置图形
        GeoPackageGeometryData geoPackageGeometryData = new GeoPackageGeometryData();
        String shpGeometryFromWkt = shpFeature.GetGeometryRef().ExportToWkt();
        SpatialReferenceCompact spatialReferenceCompact = new SpatialReferenceCompact(shpFeature.GetGeometryRef().GetSpatialReference());
        String transformedGeometryWkt = geometryDataPolicy.getTransformedGeometryWkt(shpGeometryFromWkt, spatialReferenceCompact);
        geoPackageGeometryData.setGeometryFromWkt(transformedGeometryWkt);
        featureRow.setGeometry(geoPackageGeometryData);
        //给新行设置属性
        int fieldCount = shpFeature.GetFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            org.gdal.ogr.FieldDefn fieldDefn = shpFeature.GetFieldDefnRef(i);
            String fName = fieldDefn.GetName();
            if (Shp_.SHP_PRIMARY_KEY.equalsIgnoreCase(fName) || GeoPackageQuick.COL_GEOMETRY.equalsIgnoreCase(fName)) {
                //忽略fid列,忽略geometry列
                continue;
            }
            Object fValue = getFieldValueByType(shpFeature, fieldDefn);
            featureRow.setValue(fName, fValue);
        }
        return featureRow;
    }

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Object getFieldValueByType(Feature shpFeature, org.gdal.ogr.FieldDefn gdalField) {
        String fieldName = gdalField.GetName();
        switch (gdalField.GetFieldType()) {
            case ogrConstants.OFTDate:
            case ogrConstants.OFTDateTime:
                //YYYY-MM-DDTHH:MM:SS.SSSZ
                int[] year = new int[1];
                int[] month = new int[1];
                int[] day = new int[1];
                int[] hour = new int[1];
                int[] minutes = new int[1];
                float[] seconds = new float[1];
                int[] mseconds = new int[1];
                shpFeature.GetFieldAsDateTime(fieldName, year, month, day, hour, minutes, seconds, mseconds);
                try {
                    return simpleDateFormat.parse(year[0] + "-" + month[0] + "-" + day[0] + " " + hour[0] + ":" + minutes[0] + ":" + seconds[0]);
                } catch (ParseException e) {
                    Log.e(TAG, "getFieldValueByType: GetFieldAsDateTime" + e.getMessage());
                }
            case ogrConstants.OFTBinary:
                return shpFeature.GetFieldAsBinary(fieldName);
            case ogrConstants.OFTInteger:
                return shpFeature.GetFieldAsInteger(fieldName);
            case ogrConstants.OFTInteger64:
                return shpFeature.GetFieldAsInteger64(fieldName);
            case ogrConstants.OFTReal:
                return shpFeature.GetFieldAsDouble(fieldName);
            case ogrConstants.OFTString:
            case ogrConstants.OFTWideString:
            default:
                return shpFeature.GetFieldAsString(fieldName);
        }
    }

    //将shp的字段定义转换为geopackge的字段自定
    private List<FieldDefn> getSfFieldsFromShpLayer(FeatureDefn gdalFeatureDefn, int gdalGeometryType) {
        List<FieldDefn> sfFieldDefns = new ArrayList<>();
        //默认主键字段
        FieldDefn sfIdDefine = FieldDefn.create(GeoPackageQuick.COL_PRIMARY_KEY, GeoPackageDataType.INTEGER, null);
        sfFieldDefns.add(sfIdDefine);
        //图形字段
        FieldDefn sfGeoDefine = FieldDefn.create(GeoPackageQuick.COL_GEOMETRY, GeoPackageDataType.BLOB, matchGeopackageGeometryType(gdalGeometryType));
        sfFieldDefns.add(sfGeoDefine);
        //数据字段
        int fieldCount = gdalFeatureDefn.GetFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            org.gdal.ogr.FieldDefn field = gdalFeatureDefn.GetFieldDefn(i);
            String gdalFieldName = field.GetName();
            if (!GeoPackageQuick.COL_PRIMARY_KEY.equalsIgnoreCase(gdalFieldName)) {
                int gdalFieldType = field.GetFieldType();
                sfFieldDefns.add(FieldDefn.create(gdalFieldName, matchGeopackageFieldType(gdalFieldType), null));
            }
        }
        return sfFieldDefns;
    }

    /**
     * 使用shp的字段类型匹配geopackage的字段类型
     * int OFTInteger = 0;
     * int OFTIntegerList = 1;
     * int OFTReal = 2;
     * int OFTRealList = 3;
     * int OFTString = 4;
     * int OFTStringList = 5;
     * int OFTWideString = 6;
     * int OFTWideStringList = 7;
     * int OFTBinary = 8;
     * int OFTDate = 9;
     * int OFTTime = 10;
     * int OFTDateTime = 11;
     * int OFTInteger64 = 12;
     * int OFTInteger64List = 13;
     * int OFSTNone = 0;
     * int OFSTBoolean = 1;
     * int OFSTInt16 = 2;
     * int OFSTFloat32 = 3;
     * int OJUndefined = 0;
     * int OJLeft = 1;
     * int OJRight = 2;
     *
     * @param gdalFieldType
     * @return
     */
    private GeoPackageDataType matchGeopackageFieldType(int gdalFieldType) {
        switch (gdalFieldType) {
            case ogrConstants.OFTDate:
            case ogrConstants.OFTDateTime:
                return GeoPackageDataType.DATETIME;
            case ogrConstants.OFTBinary:
                return GeoPackageDataType.BLOB;
            case ogrConstants.OFTInteger:
            case ogrConstants.OFTInteger64:
                return GeoPackageDataType.INTEGER;
            case ogrConstants.OFTString:
            case ogrConstants.OFTWideString:
                return GeoPackageDataType.TEXT;
            case ogrConstants.OFTReal:
                return GeoPackageDataType.DOUBLE;
        }
        return GeoPackageDataType.TEXT;
    }

    /**
     * 使用shp的图形类型匹配geopackage的图形类型
     *
     * @param gdalGeometryType shp gdal处理包中的geomType
     * @return
     */
    private GeometryType matchGeopackageGeometryType(int gdalGeometryType) {
        switch (gdalGeometryType) {
            case ogrConstants.wkbPoint:
                return GeometryType.POINT;
            case ogrConstants.wkbMultiPoint:
                return GeometryType.MULTIPOINT;
            case ogrConstants.wkbLineString:
            case ogrConstants.wkbMultiLineString:
                return GeometryType.MULTILINESTRING;
            case ogrConstants.wkbPolygon:
            case ogrConstants.wkbMultiPolygon:
                return GeometryType.MULTIPOLYGON;
        }
        return GeometryType.MULTIPOLYGON;
    }
}
