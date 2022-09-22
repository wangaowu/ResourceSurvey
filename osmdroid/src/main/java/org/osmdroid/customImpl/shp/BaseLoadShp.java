package org.osmdroid.customImpl.shp;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Feature;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/2/18 10:58
 */
public class BaseLoadShp {

    protected File shpFile;
    private DataSource dataSource;

    static {
        ogr.RegisterAll();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");
    }

    public BaseLoadShp(File shpFile) throws Exception {
        this.shpFile = shpFile;
        dataSource = openDataSource();
        if (dataSource == null) {
            throw new Exception("[BaseLoadShp] : datasourse 加载失败! ");
        }
    }

    private DataSource openDataSource() {
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName("ESRI Shapefile");
        Vector<String> options = new Vector<>();
        options.add("ENCODING=UTF-8");
        return oDriver.Open(shpFile.getAbsolutePath(), 1);
    }

    /**
     * 获取datasource
     *
     * @return
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 读取shp的Layer图层
     *
     * @return
     */
    public Layer readLayer() {
        return readLayer(0);
    }

    /**
     * 读取shp的Layer图层
     *
     * @return
     */
    public Layer readLayer(int layerIndex) {
        return dataSource.GetLayer(layerIndex);
    }

    /**
     * 获取所有的feature行
     *
     * @return
     */
    public List<Feature> getAllFeatures(Layer layer) {
        List<Feature> shpFeatures = new ArrayList<>();
        long featureCount = layer.GetFeatureCount();
        for (int i = 0; i < featureCount; i++) {
            shpFeatures.add(layer.GetFeature(i));
        }
        return shpFeatures;
    }

}
