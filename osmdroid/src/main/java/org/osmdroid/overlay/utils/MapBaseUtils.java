package org.osmdroid.overlay.utils;

import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;

import java.util.List;

/**
 * 类功能：地图工具类
 *
 * @author gwwang
 * @date 2022/2/23 14:03
 */
public class MapBaseUtils extends MapOverlayUtils {
    /**
     * 配置地图的 旋转、缩放大小限制、基础功能图层
     *
     * @param mapView
     */
    public static void initConfig(MapView mapView) {
        mapView.setDestroyMode(false);
        //禁用缩放按钮
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        //禁用repeat
        mapView.setHorizontalMapRepetitionEnabled(false);
        mapView.setVerticalMapRepetitionEnabled(false);
        //范围限定
        mapView.setScrollableAreaLimitDouble(BOUNDING_CHINA);
        mapView.setMinZoomLevel(MIN_ZOOM_LEVEL_CHINA);
        mapView.setMaxZoomLevel(MAX_ZOOM_LEVEL);
        //禁用旋转
        new RotateTouchProcessor(mapView).disableRotate();
        //启用多指手势
        mapView.setMultiTouchControls(true);
    }

    /**
     * 缩放所有图层
     */
    public static void autoZoom(MapView mapView) {
        List<PackageOverlay> mapPackageOverlays = getMapVisibleGPKGFoldOverlays(mapView);
        if (ListUtils.isEmpty(mapPackageOverlays)) {
            //没有图层时，缩放到中国默认层级
            mapView.getController().setCenter(BOUNDING_CHINA.getCenterWithDateLine());
            mapView.getController().zoomTo(MIN_ZOOM_LEVEL_CHINA);
            mapView.postInvalidateDelayed(10);
            return;
        }
        //有图层时，全图所有图层
        try {
            BoundingBox boundingBox = mapPackageOverlays.get(0).getBounds();
            for (PackageOverlay mapPackageOverlay : mapPackageOverlays) {
                boundingBox = boundingBox.concat(mapPackageOverlay.getBounds());
            }
            mapView.zoomToBoundingBox(boundingBox, true, DEFAULT_BOX_PADDING);
        } catch (Exception e) {
            //边界异常时，缩放到中国默认层级
            mapView.getController().setCenter(BOUNDING_CHINA.getCenterWithDateLine());
            mapView.getController().zoomTo(MIN_ZOOM_LEVEL_CHINA);
        }
    }
}
