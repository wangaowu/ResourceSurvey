package com.bytemiracle.resourcesurvey.giscommon;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.resourcesurvey.cache_greendao.DBRasterLayerDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.dbbean.DBRasterLayer;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/21 8:58
 */
public class Raster_ {
    /**
     * 将raster路径关联到工程下
     */
    public static void addRaster(String rasterPath) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        DBRasterLayerDao dbRasterLayerDao = GreenDaoManager.getInstance().getDaoSession().getDBRasterLayerDao();
        DBRasterLayer dbRasterLayer = new DBRasterLayer(null, openingProject.getId(), rasterPath);
        dbRasterLayerDao.insertOrReplace(dbRasterLayer);
    }

    /**
     * 删除栅格文件
     *
     * @param rasterPath
     * @return
     */
    public static boolean removeRaster(String rasterPath) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        DBRasterLayerDao dbRasterLayerDao = GreenDaoManager.getInstance().getDaoSession().getDBRasterLayerDao();
        DBRasterLayer waitDel = dbRasterLayerDao.queryBuilder().where(DBRasterLayerDao.Properties.ProjectId.eq(openingProject.getId()),
                DBRasterLayerDao.Properties.FilePath.eq(rasterPath)).unique();
        dbRasterLayerDao.delete(waitDel);
        return true;
    }

    /**
     * 获取栅格文件
     *
     * @return
     */
    public static List<String> getRasterPaths() {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        openingProject.resetDbRasterLayers();
        return openingProject.getDbRasterLayers().stream()
                .map(dbRasterLayer -> dbRasterLayer.getFilePath())
                .collect(Collectors.toList());
    }

    /**
     * 是否已经存在栅格文件
     *
     * @return
     */
    public static boolean isExist(String tiffPath) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        DBRasterLayerDao dbRasterLayerDao = GreenDaoManager.getInstance().getDaoSession().getDBRasterLayerDao();
        DBRasterLayer waitDel = dbRasterLayerDao.queryBuilder().where(DBRasterLayerDao.Properties.ProjectId.eq(openingProject.getId()),
                DBRasterLayerDao.Properties.FilePath.eq(tiffPath)).unique();
        return waitDel != null && waitDel.getId() != null;
    }

    /**
     * 添加rasterLayer图层到地图
     *
     * @param rasterPath
     */
    public static void addRasterLayer2Map(String rasterPath, CommonAsyncListener loadCompleteListener) {
//        ArcGISMap map = MapElementsHolder.getMapView().getMap();
//        // create a raster layer
//        com.esri.arcgisruntime.raster.Raster raster = new com.esri.arcgisruntime.raster.Raster(rasterPath);
//        RasterLayer rasterLayer = new RasterLayer(raster);
//        if (loadCompleteListener != null) {
//            rasterLayer.loadAsync();
//            rasterLayer.addDoneLoadingListener(() -> loadCompleteListener.doSomething(null));
//        }
//        // add the raster as an operational layer
//        map.getBasemap().getBaseLayers().add(rasterLayer);
//        // reset render params
//        updateRender(rasterLayer);
    }

    private static void updateRender(Object rasterLayer) {
//        MinMaxStretchParameters stretchParameters = new MinMaxStretchParameters(
//                Arrays.asList((double) 0, (double) 0, (double) 0),
//                Arrays.asList((double) 255, (double) 255, (double) 255));
//        RGBRenderer rgbRenderer = new RGBRenderer(stretchParameters, Arrays.asList(0, 1, 2), null, true);
//        rasterLayer.setRasterRenderer(rgbRenderer);
    }

}
