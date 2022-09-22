package org.osmdroid.customImpl.shp;

import android.text.TextUtils;
import android.util.Log;

import com.bytemiracle.base.framework.utils.XToastUtils;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Geometry;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.gdal.osr.SpatialReference;
import org.osmdroid.customImpl.convert.transform.SpatialReferenceCompact;
import org.osmdroid.customImpl.convert.transform.inter.GeometryDataPolicy;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureColumns;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：有关shp的工具类
 *
 * @author gwwang
 * @date 2021/5/27 13:51
 */
public class GDALUtils {
    private static final String TAG = "ShpUtils";
    //所有浮点数据的精度
    private static final int DATA_SCALE = 4;

    private static final String WGS84WKT = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]";

    static {
        ogr.RegisterAll();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");
    }

    /**
     * 获取feature的数量
     *
     * @param shpPath shp文件绝对路径
     */
    public static String getShpFeatureCount(String shpPath) {
        DataSource dataSource = loadShpData(shpPath, 0);
        if (dataSource != null) {
            Layer layer = dataSource.GetLayer(0);
            return "" + layer.GetFeatureCount();
        }
        return "err";
    }

    /**
     * 获取shp的layer定义
     *
     * @param shpPath shp文件绝对路径
     */
    public static Layer getShpLayer(String shpPath) {
        DataSource dataSource = loadShpData(shpPath, 0);
        if (dataSource != null) {
            return dataSource.GetLayer(0);
        }
        return null;
    }

    /**
     * 新增shp字段
     *
     * @param shpPath shp文件绝对路径
     * @param fields  字段
     */
    public static boolean addShpField(String shpPath, List<FieldDefn> fields) {
        DataSource dataSource = loadShpData(shpPath, 1);
        if (dataSource != null) {
            //新增属性字段
            Layer layer = dataSource.GetLayer(0);
            for (FieldDefn fieldDefn : fields) {
                if (containsField(layer, fieldDefn.GetName())) {
                    XToastUtils.info("已有同名字段，新增失败!");
                    return false;
                }
                layer.CreateField(fieldDefn, 1);
            }
            sink2TableFile(dataSource, layer);
            return true;
        }
        return false;
    }

    /**
     * 删除shp字段
     *
     * @param shpPath shp文件绝对路径
     */
    public static void deleteShpField(String shpPath, String shpField) {
        DataSource dataSource = loadShpData(shpPath, 1);
        if (dataSource != null) {
            Layer layer = dataSource.GetLayer(0);
            if (!containsField(layer, shpField)) {
                XToastUtils.info("没有该字段!");
                return;
            }
            FeatureDefn fDefn = layer.GetLayerDefn();
            for (int i = 0; i < fDefn.GetFieldCount(); i++) {
                FieldDefn fieldDefn = fDefn.GetFieldDefn(i);
                if (fieldDefn.GetName().equals(shpField)) {
                    layer.DeleteField(i);
                    break;
                }
            }
            sink2TableFile(dataSource, layer);
        }
    }

    /**
     * 修改shp字段(暂时不可用)
     *
     * @param shpPath shp文件绝对路径
     */
    public static void alterShpField(String shpPath, String shpField) {
        DataSource dataSource = loadShpData(shpPath, 1);
        if (dataSource != null) {
            Layer layer = dataSource.GetLayer(0);
            if (!containsField(layer, shpField)) {
                XToastUtils.info("没有该字段!");
                return;
            }
            FeatureDefn fieldController = layer.GetLayerDefn();
            int fieldIndex = fieldController.GetFieldIndex(shpField);
            FieldDefn fieldDefn1 = fieldController.GetFieldDefn(fieldIndex);
            layer.AlterFieldDefn(fieldIndex, fieldDefn1, 0);
            sink2TableFile(dataSource, layer);
        }
    }

    /**
     * 加载layer
     *
     * @param shpPath
     * @return
     */
    private static DataSource loadShpData(String shpPath, int mode) {
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName("ESRI Shapefile");
        if (oDriver == null) {
            XToastUtils.info("ogr 驱动不可用！");
            return null;
        }
        // 打开数据源 (1必须，表示读写)
        DataSource oDS = oDriver.Open(shpPath, mode);
        if (oDS == null || oDS.GetLayerCount() == 0) {
            return null;
        }
        return oDS;
    }

    /**
     * 是否已有字段
     *
     * @param layer
     * @param fieldName
     * @return
     */
    private static boolean containsField(Layer layer, String fieldName) {
        int fieldCount = layer.GetLayerDefn().GetFieldCount();
        for (int i = 0; i < fieldCount; i++) {
            if (fieldName.equalsIgnoreCase(layer.GetLayerDefn().GetFieldDefn(i).GetName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 创建算法生产的边界矢量图
     *
     * @param shpFilePath shp文件路径
     * @param layerName   图层名称
     * @param prjWKT
     * @param geomType    图层类型
     * @param fields      字段列表
     * @return
     */
    public static boolean createNewShpFile(String shpFilePath, String layerName, String prjWKT, int geomType, List<FieldDefn> fields) {
        // 步骤1、创建数据源
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName("ESRI Shapefile");

        Vector<String> options = new Vector<>();
        options.add("ENCODING=UTF-8");

        DataSource oDS = oDriver.CreateDataSource(shpFilePath, options);
        if (oDS == null) {
            Log.e(TAG, "创建矢量Shp文件失败: " + shpFilePath);
            return false;
        }
        //步骤2、创建图层，并添加坐标系，创建一个多边形图层(wkbGeometryType.wkbUnknown,存放任意几何特征)
        SpatialReference srs = new SpatialReference();
        Layer oLayer = oDS.CreateLayer(layerName, srs, geomType, options);
        if (oLayer == null) {
            Log.e(TAG, "表文件创建失败！");
            return false;
        }
        //步骤3、创建空间坐标系
        //实质步骤2内的坐标系是空的(shp文件指定坐标系，仅写入wkt到xx.prj内即可)
        String esriSpatialReferenceWkt = new SpatialReferenceCompact(prjWKT).getEsriSpatialReferenceWkt();
        if (!Shp_.FileUtils.writeShpPrj(esriSpatialReferenceWkt, shpFilePath)) {
            return false;
        }
        //步骤4、下面创建属性表
        for (FieldDefn field : fields) {
            // 步骤5、将创建的属性表添加到图层中
            oLayer.CreateField(field, 1);
        }
        //步骤5、创建默认要素(因为没有extent的话shp不可用)
        Feature defaultFeature = new Feature(oLayer.GetLayerDefn());
        String wkt = null;
        switch (geomType) {
            case ogr.wkbPoint:
                wkt = "POINT(109.8 34.7)";
                break;
            case ogr.wkbLineString:
                wkt = "LINESTRING (109.8 34.7,109.8 34.8)";
                break;
            case ogr.wkbPolygon:
                wkt = "POLYGON ((109.8 34.7,109.9 34.7,109.9 34.8,109.8 34.8))";
                break;
        }
        defaultFeature.SetGeometry(new Geometry(geomType));//Geometry.CreateFromWkt(wkt)
        //步骤6、设置属性值
        //defaultFeature.SetField();
        //步骤7、将特征要素添加到图层中
        int fid = oLayer.CreateFeature(defaultFeature);//必须先插入默认删除默认，不然创建shp失败
        oLayer.DeleteFeature(fid);//必须先插入默认删除默认，不然创建shp失败
        try {
            sink2TableFile(oDS, oLayer);
        } catch (Exception r) {
            Log.d(TAG, "createNewShpFile() called with: " + r.getMessage());
            Log.d(TAG, "createNewShpFile() called with: " + gdal.GetLastErrorMsg());
        }
        return true;
    }

    /**
     * 添加新行
     *
     * @param dateSource         shp的数据源
     * @param layerName          shp图层名称
     * @param gpkgFeatureRow     gpkg文件类型的row行
     * @param aliasTable         <aliasName,FieldName>
     * @param geometryDataPolicy 图形数据转换策略
     * @param targetPrjWkt       目标数据坐标系
     * @return
     */
    public static int addNewRowFromGPKG(DataSource dateSource, String layerName, FeatureRow gpkgFeatureRow, Map<String, String> aliasTable, GeometryDataPolicy geometryDataPolicy, String targetPrjWkt) {
        Layer layer;
        if (!TextUtils.isEmpty(layerName)) {
            layer = dateSource.GetLayer(layerName);
        } else {
            layer = dateSource.GetLayer(0);
        }
        String srcWkt = gpkgFeatureRow.getGeometry().getWkt();
        String targetWkt = geometryDataPolicy.getTransformedGeometryWkt(srcWkt, new SpatialReferenceCompact(targetPrjWkt));
        Feature newFeature = new Feature(layer.GetLayerDefn());
        newFeature.SetGeometry(Geometry.CreateFromWkt(targetWkt));
        FeatureColumns columns = gpkgFeatureRow.getColumns();
        for (FeatureColumn column : columns.getColumns()) {
            if (column.isGeometry() || column.isPrimaryKey()) {
                continue;
            }
            String columnName = column.getName();
            Object columnValue = gpkgFeatureRow.getValue(columnName);
            String shpColName = aliasTable.get(columnName);
            switch (column.getDataType()) {
                case REAL:
                case FLOAT:
                case DOUBLE:
                    newFeature.SetField(shpColName, (double) columnValue);
                    break;
                case TINYINT:
                case MEDIUMINT:
                case SMALLINT:
                    newFeature.SetField(shpColName, (int) columnValue);
                    break;
                case INT:
                case INTEGER:
                    newFeature.SetField(shpColName, (long) columnValue);
                    break;
                case DATE:
                case DATETIME:
                case TEXT:
                default:
                    newFeature.SetField(shpColName, String.valueOf(columnValue));
                    break;
            }
        }

        return layer.CreateFeature(newFeature);
    }

    /**
     * 修正边界
     *
     * @param shpPath
     */
    public static void fixExtent(String shpPath) {
        DataSource dataSource = loadShpData(shpPath, 1);
        Layer layer = dataSource.GetLayer(0);
        sink2TableFile(dataSource, layer);
    }

    /**
     * 更新行内属性
     *
     * @param dateSource
     * @param feature
     */
    public static void updateRow(DataSource dateSource, org.gdal.ogr.Feature feature) {
        dateSource.GetLayer(0).SetFeature(feature);
        sink2TableFile(dateSource, dateSource.GetLayer(0));
    }

    public static DataSource getDateSource(String shpPath) {
        return loadShpData(shpPath, 1);
    }

    /**
     * 保存shp信息
     *
     * @param layer
     * @return
     */
    private static void sink2TableFile(DataSource dataSource, Layer layer) {
        dataSource.ExecuteSQL("RECOMPUTE EXTENT ON " + layer.GetName());
        layer.SyncToDisk();
        dataSource.SyncToDisk();
        dataSource.ExecuteSQL("REPACK " + layer.GetName(), null, "");
    }
}