package org.osmdroid.customImpl.shp;

import android.util.Log;

import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.bean.options.OsmRenderOption;
import org.osmdroid.overlay.render.OsmdroidMapRender;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.List;

/**
 * 类功能：加载shp文件到地图
 *
 * @author gwwang
 * @date 2021/12/18 14:28
 */
public class LoadShpImpl extends BaseLoadShp {
    private static final String TAG = "LoadShpImpl";

    private MapView mapView;

    /**
     * 构造方法
     *
     * @param mapView
     * @param shpFile
     */
    public LoadShpImpl(MapView mapView, File shpFile) throws Exception {
        super(shpFile);
        this.mapView = mapView;
    }

    /**
     * 执行加载
     *
     * @param renderOption 渲染配置
     * @return
     */
    public List<OverlayWithIW> execute(OsmRenderOption renderOption, String overlayName) {
        return execute(renderOption, overlayName, null);
    }

    /**
     * 执行加载
     *
     * @param renderOption  渲染配置
     * @param clickListener 点击事件
     * @return
     */
    public List<OverlayWithIW> execute(OsmRenderOption renderOption, String overlayName, Object clickListener) {
        try {
            List<OverlayWithIW> shapeList = ShapeConverter.convert(mapView, shpFile, renderOption);
            for (Overlay overlay : shapeList) {
                if (overlay instanceof Marker) {
                    dispatchMarkerClickListener((Marker) overlay, clickListener);
                } else if (overlay instanceof Polygon) {
                    dispatchPolygonClickListener((Polygon) overlay, clickListener);
                } else if (overlay instanceof Polyline) {
                    dispatchPolylineClickListener((Polyline) overlay, clickListener);
                }
            }
            new OsmdroidMapRender(mapView, renderOption, overlayName, PackageOverlayInfo.Category.SHP, PackageOverlayInfo.OSMGeometryType.POLYGON).addOverlay(shapeList);
            return shapeList;
        } catch (Exception e) {
            Log.e(TAG, "convert: " + e.getMessage());
            return null;
        }
    }

    private void dispatchPolylineClickListener(Polyline overlay, Object clickListener) {
        if (clickListener instanceof Polyline.OnClickListener) {
            overlay.setOnClickListener((Polyline.OnClickListener) clickListener);
        } else {
            //将默认的点击事件覆盖掉
            //API逻辑并未实现线条的点击事件
        }
    }

    private void dispatchPolygonClickListener(Polygon overlay, Object clickListener) {
        if (clickListener instanceof Polygon.OnClickListener) {
            overlay.setOnClickListener((Polygon.OnClickListener) clickListener);
        } else {
            //将默认的点击事件覆盖掉
            overlay.setOnClickListener((polygon, mapView, eventPos) -> true);
        }
    }

    private void dispatchMarkerClickListener(Marker overlay, Object clickListener) {
        if (clickListener instanceof Marker.OnMarkerClickListener) {
            overlay.setOnMarkerClickListener((Marker.OnMarkerClickListener) clickListener);
        } else {
            //将默认的点击事件覆盖掉
            overlay.setOnMarkerClickListener((marker, mapView) -> true);
        }
    }
}
