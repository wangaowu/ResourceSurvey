package org.osmdroid.customImpl.geopackage;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/24 16:17
 */

import android.util.Log;

import com.xuexiang.xutil.file.FileUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.contents.ContentsDataType;
import mil.nga.geopackage.features.columns.GeometryColumns;
import mil.nga.geopackage.features.columns.GeometryColumnsDao;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.features.user.FeatureTable;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.sf.GeometryEnvelope;

/**
 * 类功能：编辑geopackage内容
 *
 * @author gwwang
 * @date 2021/6/24 16:17
 */
public class EditGeoPackage_ {
    private static final String TAG = "EditGeoPackage_";

    private GeoPackage geoPackage;

    public EditGeoPackage_(GeoPackage geoPackage) {
        this.geoPackage = geoPackage;
    }

    /**
     * 获取默认的featureTable名称
     *
     * @param isCreate 是否创建模式(true则为xx_+1，false为xx_0)
     * @return
     */
    public String getDefaultTableName(boolean isCreate) {
        String nameNoExtension = FileUtils.getFileNameNoExtension(geoPackage.getPath());
        return nameNoExtension + "_" + (isCreate ? geoPackage.getFeatureTables().size() : geoPackage.getFeatureTables().size() - 1);
    }

    /**
     * 创建featuretable
     *
     * @param fieldDefnList          字段定义
     * @param spatialReferenceSystem 坐标系指定
     * @return
     * @throws Exception
     */
    public int addFeatureTable(List<FieldDefn> fieldDefnList,
                               SpatialReferenceSystem spatialReferenceSystem) throws Exception {
        String tableName = getDefaultTableName(true);
        return addFeatureTableSpatialName(tableName, fieldDefnList, spatialReferenceSystem);
    }

    /**
     * 获取feature表
     *
     * @return
     */
    public FeatureDao getFeatureDao(String featureTableName) {
        return geoPackage.getFeatureDao(featureTableName);
    }

    /**
     * 创建featuretable
     *
     * @param featureTableName       表名称
     * @param fieldDefnList          字段定义
     * @param spatialReferenceSystem 坐标系指定 (实质只支持WGS84)
     * @return
     * @throws Exception
     */
    public int addFeatureTableSpatialName(String featureTableName, List<FieldDefn> fieldDefnList,
                                          SpatialReferenceSystem spatialReferenceSystem) throws Exception {
        ContentsDao contentsDao = geoPackage.getContentsDao();
        GeometryColumnsDao geomColumnsDao = geoPackage.getGeometryColumnsDao();

        int rowResult = -1;

        if (geoPackage.getFeatureTables().contains(featureTableName)) {
            Log.e(TAG, "createFeatureTable: 创建失败，已有列 " + featureTableName);
            return rowResult;
        }

        geoPackage.beginTransaction();

        //创建featuretable
        List<FeatureColumn> featureColumns = new ArrayList<>();
        for (FieldDefn fieldDefn : fieldDefnList) {
            FeatureColumn column;
            if (GeoPackageQuick.COL_PRIMARY_KEY.equals(fieldDefn.fieldName)) {
                //主键列
                column = FeatureColumn.createPrimaryKeyColumn(GeoPackageQuick.COL_PRIMARY_KEY, true);
            } else {
                //创建属性列
                column = FeatureColumn.createColumn(fieldDefn.fieldName, fieldDefn.fieldType);
                if (fieldDefn.isGeometryCol) {
                    // geometry列
                    column.setType(fieldDefn.geometryType.getName());
                }
            }
            featureColumns.add(column);
        }
        geoPackage.createFeatureTable(new FeatureTable(featureTableName, featureColumns));

        //将featureTable同步到contents
        Contents contents = new Contents();
        contents.setTableName(featureTableName);
        contents.setDataType(ContentsDataType.FEATURES);
        contents.setIdentifier(featureTableName);
        contents.setDescription("");
        contents.setLastChange(new Date());
        contents.setSrs(spatialReferenceSystem);
        contents.setBoundingBox(GeoPackageQuick.BOX_SHAANXI);//应该等价于4个点
        contentsDao.create(contents);

        //将geometry的col列让GeometryColumns表进行管理
        FieldDefn geometryCol = FieldDefn.findGeometryCol(fieldDefnList);
        GeometryColumns gcl = new GeometryColumns();
        gcl.setTableName(featureTableName);
        gcl.setGeometryType(geometryCol.geometryType);
        gcl.setColumnName(geometryCol.fieldName);
        gcl.setContents(contents);
        gcl.setSrs(spatialReferenceSystem);
        gcl.setM(new Integer(0).byteValue());
        gcl.setZ(new Integer(0).byteValue());
        rowResult = geomColumnsDao.create(gcl);

        geoPackage.endTransaction();
        return rowResult;
    }

    /**
     * 移除featureTable
     *
     * @param featureTableName
     * @return
     * @throws Exception
     */
    public void delFeatureTableSpatialName(String featureTableName) {
        geoPackage.deleteTable(featureTableName);
    }

    /**
     * 更新边界
     *
     * @param featureTableName featureTable的名称
     * @param geometryEnvelop  arcgis图形
     * @return
     * @throws Exception
     */
    public long updateExtent(String featureTableName, GeometryEnvelope geometryEnvelop) throws Exception {
        long rowResult = -1;
        geoPackage.beginTransaction();

        BoundingBox unionBounds;
        BoundingBox geometryBounds = GeoPackageQuick.fromEnvelop(geometryEnvelop);
        BoundingBox existBoundingBox = GeoPackageQuick.getRead(geoPackage).queryExtent(featureTableName);
        if (existBoundingBox.equals(GeoPackageQuick.BOX_SHAANXI)) {
            unionBounds = geometryBounds;
        } else {
            //联合旧的边界
            unionBounds = existBoundingBox.union(geometryBounds);
        }
        //更新边界
        ContentsDao contentsDao = geoPackage.getContentsDao();
        Contents tableContents = contentsDao.getContents(ContentsDataType.FEATURES).stream()
                .filter(contents -> contents.getIdentifier().equals(featureTableName))
                .findFirst().get();
        tableContents.setBoundingBox(unionBounds);
        rowResult = contentsDao.update(tableContents);

        geoPackage.endTransaction();
        return rowResult;
    }

    /**
     * 添加featureTable字段
     *
     * @param featureTableName
     * @param fieldDefn
     */
    public boolean addFeatureTableCol(String featureTableName, FieldDefn fieldDefn) {
        boolean result = false;

        geoPackage.beginTransaction();
        //新增列字段
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        boolean hasColumn = Arrays.stream(featureDao.getColumnNames()).anyMatch(s -> s.equals(fieldDefn.fieldName));
        if (!hasColumn) {
            String name = fieldDefn.fieldName;
            FeatureColumn newColumn = FeatureColumn.createColumn(name, fieldDefn.fieldType, false, fieldDefn.defaultValue);
            featureDao.addColumn(newColumn);
            result = true;
        }
        geoPackage.endTransaction();
        geoPackage.close();
        return result;
    }

    /**
     * 删除featureTable字段
     *
     * @param columnName
     * @return
     */
    public boolean delFeatureTableCol(String featureTableName, String columnName) {
        boolean result = false;

        geoPackage.beginTransaction();
        //新增列字段
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        boolean hasColumn = Arrays.stream(featureDao.getColumnNames()).anyMatch(s -> s.equals(columnName));
        if (hasColumn) {
            featureDao.dropColumn(columnName);
            result = true;
        }
        geoPackage.endTransaction();
        geoPackage.close();
        return result;
    }

    /**
     * 重命名featureTable字段
     *
     * @param featureTableName
     * @param columnName
     * @param newColumnName
     * @return
     */
    public boolean renameFeatureTableCol(String featureTableName, String columnName, String newColumnName) {
        boolean result = false;

        geoPackage.beginTransaction();
        //新增列字段
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        boolean hasColumn = Arrays.stream(featureDao.getColumnNames()).anyMatch(s -> s.equals(columnName));
        if (hasColumn) {
            featureDao.renameColumn(columnName, newColumnName);
            result = true;
        }
        geoPackage.endTransaction();
        return result;
    }

    /**
     * 添加feature(新增图形)
     *
     * @param featureTableName
     * @param featureRow
     * @return
     */
    public long addFeature(String featureTableName, FeatureRow featureRow) {
        long rowResult = -1;
        geoPackage.beginTransaction();

        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        rowResult = featureDao.insert(featureRow);

        geoPackage.endTransaction();
        return rowResult;
    }

    /**
     * 更新feature
     *
     * @param featureRow
     * @return
     * @throws Exception
     */
    public long updateFeature(FeatureRow featureRow) throws Exception {
        long rowResult = -1;
        geoPackage.beginTransaction();

        String featureTableName = featureRow.getTable().getTableName();
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        rowResult = featureDao.update(featureRow);

        geoPackage.endTransaction();
        return rowResult;
    }
}
