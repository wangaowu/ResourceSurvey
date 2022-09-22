package org.osmdroid.overlay.utils;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.osmdroid.overlay.OnlineMapHolder;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.overlay.render.IWMarker;
import org.osmdroid.overlay.render.IWPolygon;
import org.osmdroid.overlay.render.IWPolyline;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 类功能：基础图层处理类，包含：
 * 1.folderOverlay
 * 2.tileOverlay
 * 3.rotationOverlay
 * 4.minimapOverlay
 * 等一些以整个叠加层存在的形式图层
 *
 * @author gwwang
 * @date 2021/6/8 14:35
 */
public class MapOverlayUtils implements MapConstant {
    private static final String TAG = "MapOverlayUtils";

    /**
     * 获取旋转图层
     *
     * @param mapView
     * @return
     */
    public static RotationGestureOverlay ensureRotationGestureOverlay(MapView mapView) {
        Optional<RotationGestureOverlay> hasOptional = mapView.getOverlayManager().overlays()
                .stream()
                .filter(overlay -> overlay instanceof RotationGestureOverlay)
                .map(overlay -> (RotationGestureOverlay) overlay)
                .findFirst();
        RotationGestureOverlay rotationGestureOverlay;
        if (hasOptional.isPresent()) {
            rotationGestureOverlay = hasOptional.get();
        } else {
            rotationGestureOverlay = new RotationGestureOverlay(mapView);
            mapView.getOverlayManager().overlays().add(rotationGestureOverlay);
        }
        return rotationGestureOverlay;
    }

    /**
     * 加载瓦片影像
     */
    public static void loadTileLayers(MapView mapView, List<OnlineMapHolder.OnlineMap> overlays) {
        Optional<Overlay> optionalOverlay = MapOverlayUtils.getMapLabelOverlay(mapView);
        optionalOverlay.ifPresent(overlay -> mapView.getOverlayManager().overlays().remove(overlay));
        for (OnlineMapHolder.OnlineMap overlay : overlays) {
            if (overlay.getVisible()) {
                mapView.setTileSource(overlay.getImgSource());
            }
            TilesOverlay labelOverlay = overlay.getLabelOverlay();
            if (overlay.getShowLabel() && labelOverlay != null) {
                mapView.getOverlayManager().overlays().add(0, labelOverlay);
            }
        }
        mapView.invalidate();
    }


    /**
     * 获取地图的矢量图层
     *
     * @param mapView
     * @return
     */
    public static List<PackageOverlay> getMapGPKGFoldOverlays(MapView mapView) {
        return getMapPackageOverlays(mapView).stream().filter(overlay -> {
            if (overlay.getPackageOverlayInfo() != null) {
                return overlay.getPackageOverlayInfo().getOverlayCategory() == PackageOverlayInfo.Category.GEOPACKAGE;
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * 获取地图的可见矢量图层
     *
     * @param mapView
     * @return
     */
    public static List<PackageOverlay> getMapVisibleGPKGFoldOverlays(MapView mapView) {
        return getMapPackageOverlays(mapView).stream().filter(overlay -> {
            PackageOverlayInfo packageOverlayInfo = overlay.getPackageOverlayInfo();
            if (packageOverlayInfo != null && PackageOverlayInfo.Category.GEOPACKAGE == packageOverlayInfo.getOverlayCategory()) {
                return overlay.isEnabled();
            }
            return false;
        }).collect(Collectors.toList());
    }

    /**
     * 获取地图的矢量图层
     *
     * @param mapView
     * @return
     */
    public static List<PackageOverlay> getMapPackageOverlays(MapView mapView) {
        return getMapOverlays(mapView).stream()
                .filter(overlay -> overlay instanceof PackageOverlay)
                .map(overlay -> (PackageOverlay) overlay)
                .collect(Collectors.toList());
    }

    /**
     * 获取地图的可见矢量图层
     *
     * @param mapView
     * @return
     */
    public static List<PackageOverlay> getVisibleMapPackageOverlays(MapView mapView) {
        return getMapGPKGFoldOverlays(mapView).stream()
                .filter(overlay -> overlay.isEnabled())
                .collect(Collectors.toList());
    }

    /**
     * 获取地图的所有图层
     *
     * @param mapView
     * @return
     */
    public static List<Overlay> getMapOverlays(MapView mapView) {
        return mapView.getOverlayManager().overlays();
    }

    /**
     * 获取地图的注记图层
     *
     * @param mapView
     * @return
     */
    public static Optional<Overlay> getMapLabelOverlay(MapView mapView) {
        Optional<Overlay> optionalOverlay = mapView.getOverlayManager().overlays().stream()
                .filter(overlay -> overlay instanceof TilesOverlay)
                .findFirst();
        return optionalOverlay;
    }
    /**
     * 匹配图形类型的文本指示
     *
     * @param overlay
     * @return
     */
    public static String matchTypeText(PackageOverlay overlay) {
        List<Overlay> featureGeometries = overlay.getItems();
        if (ListUtils.isEmpty(featureGeometries)) {
            return "未知";
        }
        Overlay featureGeometry = featureGeometries.get(0);
        if (featureGeometry instanceof IWMarker) {
            return "点";
        } else if (featureGeometry instanceof IWPolyline) {
            return "线";
        } else if (featureGeometry instanceof IWPolygon) {
            return "多边形";
        } else {
            return "未知";
        }
    }

    /**
     * 判断该feature是否被选中
     *
     * @param layer                      哪个图层
     * @param identifyShape              判断该feature是否选中
     * @param getSelectedFeatureListener 获取到选中feature的监听器
     */
    public static void asyncGetSelected(Overlay layer, OverlayWithIW identifyShape, CommonAsyncListener<Boolean> getSelectedFeatureListener) {
//        ListenableFuture<FeatureQueryResult> selectedFeaturesAsync = layer.getSelectedFeaturesAsync();
//        selectedFeaturesAsync.addDoneListener(() -> {
//            try {
//                boolean isSelected = false;
//                FeatureQueryResult features = selectedFeaturesAsync.get();
//                for (Feature feature : features) {
//                    if (feature.getGeometry() != null && feature.getGeometry().equals(identifyShape.getGeometry())) {
//                        isSelected = true;
//                        break;
//                    }
//                }
//                getSelectedFeatureListener.doSomething(isSelected);
//            } catch (Exception e) {
//                Log.e(TAG, "asyncGetSelected: " + e.getMessage());
//                getSelectedFeatureListener.doSomething(false);
//            }
//        });
    }

    /**
     * 启用图层可选
     *
     * @param folderOverlays
     */
    public static void setOverlaySelectState(List<PackageOverlay> folderOverlays, boolean enableSelected) {
        if (!ListUtils.isEmpty(folderOverlays)) {
            for (PackageOverlay folderOverlay : folderOverlays) {
                folderOverlay.getPackageOverlayInfo().setSelectable(enableSelected);
            }
        }
    }

    /**
     * 清空高亮选中的feature
     */
    public static void clearHighlightFeature(MapView mapView) {
        List<PackageOverlay> visibleMapPackageOverlays = getVisibleMapPackageOverlays(mapView);
        if (!ListUtils.isEmpty(visibleMapPackageOverlays)) {
            for (PackageOverlay visibleMapPackageOverlay : visibleMapPackageOverlays) {
                List<Overlay> items = visibleMapPackageOverlay.getItems();
                if (!ListUtils.isEmpty(items)) {
                    for (Overlay featureGeometry : items) {
                        if (featureGeometry instanceof ISelectOverlay) {
                            ((ISelectOverlay) featureGeometry).setSelected(false);
                        }
                    }
                }
            }
        }
        mapView.invalidate();
    }

    /**
     * 移除地图的矢量图层
     *
     * @param mapView
     * @return
     */
    public static void clearMapOverlaysWithIW(MapView mapView) {
        List<PackageOverlay> mapOverlaysWithIW = getMapGPKGFoldOverlays(mapView);
        mapView.getOverlayManager().overlays().removeAll(mapOverlaysWithIW);
    }

    /**
     * 移除地图的基础图层
     *
     * @param mapView
     */
    public static void clearMapBaseOverlays(MapView mapView) {
        mapView.getOverlayManager().clear();
    }

    /**
     * 将overlay添加到地图
     *
     * @param overlay
     * @return 地图所有的 OperationalOverlays
     */
    public static void addMapViewLayer(MapView mapView, Overlay overlay) {
        mapView.getOverlayManager().add(overlay);
        mapView.zoomToBoundingBox(overlay.getBounds(), true, DEFAULT_BOX_PADDING);
    }
}
