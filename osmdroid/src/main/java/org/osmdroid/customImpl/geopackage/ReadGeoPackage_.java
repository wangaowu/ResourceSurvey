package org.osmdroid.customImpl.geopackage;


import java.util.List;
import java.util.stream.Collectors;

import mil.nga.geopackage.BoundingBox;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.contents.Contents;
import mil.nga.geopackage.contents.ContentsDao;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.geopackage.srs.SpatialReferenceSystemDao;
import mil.nga.geopackage.user.UserQuery;

/**
 * 类功能：读取geopackage内容
 *
 * @author gwwang
 * @date 2021/6/24 16:17
 */
public class ReadGeoPackage_ {
    private GeoPackage geoPackage;

    public ReadGeoPackage_(GeoPackage geoPackage) {
        this.geoPackage = geoPackage;
    }

    /**
     * 获取默认的wgs84坐标系定义
     *
     * @return
     * @throws Exception
     */
    public SpatialReferenceSystem querySpatialReferenceSystem(long srsId) throws Exception {
        SpatialReferenceSystemDao srsDao = geoPackage.getSpatialReferenceSystemDao();
        return srsDao.queryForAll().stream()
                .filter(spatialReferenceSystem -> spatialReferenceSystem.getSrsId() == srsId)
                .findFirst().get();
    }

    /**
     * 获取已有的边界
     *
     * @return
     * @throws Exception
     */
    public BoundingBox queryExtent(String featureTableName) throws Exception {
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        return featureDao.getContents().getBoundingBox();
    }

    /**
     * 获取已有的featureTable
     *
     * @return
     * @throws Exception
     */
    public Contents queryContents(String featureTableName) throws Exception {
        ContentsDao contentsDao = geoPackage.getContentsDao();
        return contentsDao.queryForEq("table_name", featureTableName).get(0);
    }

    /**
     * 查询列名
     *
     * @return
     * @throws Exception
     */
    public List<FeatureColumn> queryColumns(String featureTableName) {
        List<FeatureColumn> featureColumns = geoPackage.getFeatureDao(featureTableName).getColumns();
        //将主键排到第一位
        FeatureColumn primaryColumn = featureColumns.stream().filter(featureColumn -> featureColumn.isPrimaryKey()).findFirst().get();
        featureColumns.remove(primaryColumn);
        featureColumns.add(0, primaryColumn);
        return featureColumns;
    }

    /**
     * 查询列名 （不包含图形列）
     *
     * @return
     * @throws Exception
     */
    public List<FeatureColumn> queryColumnsWithoutGeometry(String featureTableName) {
        return queryColumns(featureTableName).stream()
                .filter(featureColumn -> !featureColumn.isGeometry())
                .collect(Collectors.toList());
    }

    /**
     * 查询一行（查询属性值）
     *
     * @param featureTableName
     * @param primaryKeyId
     * @return
     */
    public FeatureRow queryRow(String featureTableName, long primaryKeyId) {
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        String sql = "select * from " + featureTableName + " where " + queryPrimaryKey(featureTableName) + "  = ?";
        FeatureCursor cursor = featureDao.query(new UserQuery(sql, new String[]{"" + primaryKeyId}));
        try {
            while (cursor.moveToNext()) {
                FeatureRow row = cursor.getRow();
                return row;
            }
            return null;
        } catch (Exception e) {
            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * 查询主键名
     *
     * @param featureTableName
     * @return
     */
    public String queryPrimaryKey(String featureTableName) {
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        return featureDao.getColumns().stream()
                .filter(featureColumn -> featureColumn.isPrimaryKey())
                .findFirst().get().getName();
    }

    /**
     * 查询图形属性列名
     *
     * @param featureTableName
     * @return
     */
    public String queryGeometryKey(String featureTableName) {
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        return featureDao.getColumns().stream()
                .filter(featureColumn -> featureColumn.isGeometry())
                .findFirst().get().getName();
    }

    /**
     * 获取字段类型
     *
     * @param featureTableName 所处表名称
     * @param fieldName        字段名称
     * @return
     */
    public GeoPackageDataType getFieldType(String featureTableName, String fieldName) {
        FeatureDao featureDao = geoPackage.getFeatureDao(featureTableName);
        return featureDao.getColumns().stream()
                .filter(featureColumn -> featureColumn.getName().equals(fieldName))
                .findFirst().get()
                .getDataType();
    }

//        public static void queryByXX () {
//// Query Tiles
////            String tileTable = tiles.get(0);
////            TileDao tileDao = geoPackage.getTileDao(tileTable);
////            TileCursor tileCursor = tileDao.queryForAll();
////            try {
////                while (tileCursor.moveToNext()) {
////                    TileRow tileRow = tileCursor.getRow();
////                    byte[] tileBytes = tileRow.getTileData();
////                    Bitmap tileBitmap = tileRow.getTileDataBitmap();
////                    // ...
////                }
////            } finally {
////                tileCursor.close();
////            }
//
//// Index Features
//            FeatureIndexManager indexer = new FeatureIndexManager(context, geoPackage, featureDao);
//            indexer.setIndexLocation(FeatureIndexType.GEOPACKAGE);
//            int indexedCount = indexer.index();
//
//// Draw tiles from features
//            FeatureTiles featureTiles = new DefaultFeatureTiles(context, featureDao, context.getResources().getDisplayMetrics().density);
//            featureTiles.setMaxFeaturesPerTile(1000); // Set max features to draw per tile
//            NumberFeaturesTile numberFeaturesTile = new NumberFeaturesTile(context); // Custom feature tile implementation
//            featureTiles.setMaxFeaturesTileDraw(numberFeaturesTile); // Draw feature count tiles when max features passed
//            featureTiles.setIndexManager(indexer); // Set index manager to query feature indices
//            Bitmap tile = featureTiles.drawTile(2, 2, 2);
//
//            BoundingBox boundingBox = boundingBox;
//            Projection projection = ProjectionFactory.getProjection(ProjectionConstants.EPSG_WORLD_GEODETIC_SYSTEM);
//
//// URL Tile Generator (generate tiles from a URL)
//            TileGenerator urlTileGenerator = new UrlTileGenerator(context, geoPackage,
//                    "url_tile_table", "http://url/{z}/{x}/{y}.png", 1, 2, boundingBox, projection);
//            int urlTileCount = urlTileGenerator.generateTiles();
//
//// Feature Tile Generator (generate tiles from features)
//            TileGenerator featureTileGenerator = new FeatureTileGenerator(context, geoPackage,
//                    featureTable + "_tiles", featureTiles, 1, 2, boundingBox, projection);
//            int featureTileCount = featureTileGenerator.generateTiles();
//
//// Close feature tiles (and indexer)
//            featureTiles.close();

// Close database when done

}

