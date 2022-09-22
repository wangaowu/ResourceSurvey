package com.bytemiracle.resourcesurvey.osmdroid.overlay;

import com.bytemiracle.resourcesurvey.common.EventCluster;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.render.IWMarker;
import org.osmdroid.overlay.render.IWPolygon;
import org.osmdroid.overlay.render.IWPolyline;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapOverlayUtils;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类功能：地图的要素保存类(供全局使用)
 *
 * @author gwwang
 * @date 2022/2/24 15:49
 */
public class MapElementsHolder {
    //mapview对象
    protected static WeakReference<MapView> mapViewWeakReference;
    //正在高亮的图形
    protected static OverlayWithIW identifyShape;
    //正在编辑的图层
    protected static PackageOverlay editOverlay;
    //可编辑的图层
    protected static List<PackageOverlay> editableOverlays = new ArrayList<>();

    //polygon的点击事件
    private static Polygon.OnClickListener polygonClickListener = (polygon, mapView, eventPos) -> {
        if (polygon instanceof IWPolygon) {
            IWPolygon iwPolygon = (IWPolygon) polygon;
            FeatureOverlayInfo featureOverlayInfo = iwPolygon.getFeatureOverlayInfo();
            if (featureOverlayInfo != null) {
                boolean isSelectableFoldOverlay = featureOverlayInfo.getPackageOverlay().getPackageOverlayInfo().isSelectable();
                //当前图层是否可选中
                if (isSelectableFoldOverlay) {
                    if (iwPolygon.isSelected()) {
                        iwPolygon.setSelected(false);
                        MapElementsHolder.setIdentifyShape(null);
                        EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
                    } else {
                        MapOverlayUtils.clearHighlightFeature(mapViewWeakReference.get());//驱动单选
                        iwPolygon.setSelected(true);
                        MapElementsHolder.setIdentifyShape(iwPolygon);
                        EventBus.getDefault().post(new EventCluster.EventEditGeometry(iwPolygon));
                    }
                    mapViewWeakReference.get().invalidate();
                }
            }
            return true;
        }
        return false;
    };

    //marker的点击事件
    private static Marker.OnMarkerClickListener markerClickListener = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            if (marker instanceof IWMarker) {
                IWMarker iwMarker = (IWMarker) marker;
                FeatureOverlayInfo featureOverlayInfo = iwMarker.getFeatureOverlayInfo();
                if (featureOverlayInfo != null) {
                    boolean isSelectableFoldOverlay = featureOverlayInfo.getPackageOverlay().getPackageOverlayInfo().isSelectable();
                    //当前图层是否可选中
                    if (isSelectableFoldOverlay) {
                        if (iwMarker.isSelected()) {
                            iwMarker.setSelected(false);
                            MapElementsHolder.setIdentifyShape(null);
                            EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
                        } else {
                            MapOverlayUtils.clearHighlightFeature(mapViewWeakReference.get());//驱动单选
                            iwMarker.setSelected(true);
                            MapElementsHolder.setIdentifyShape(iwMarker);
                            EventBus.getDefault().post(new EventCluster.EventEditGeometry(iwMarker));
                        }
                        mapViewWeakReference.get().invalidate();
                    }
                }
                return true;
            }
            return false;
        }
    };

    //线条的点击事件
    private static Polyline.OnClickListener polylineClickListener = new Polyline.OnClickListener() {
        @Override
        public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
            if (polyline instanceof IWPolyline) {
                IWPolyline iwPolyline = (IWPolyline) polyline;
                FeatureOverlayInfo featureOverlayInfo = iwPolyline.getFeatureOverlayInfo();
                if (featureOverlayInfo != null) {
                    boolean isSelectableFoldOverlay = featureOverlayInfo.getPackageOverlay().getPackageOverlayInfo().isSelectable();
                    //当前图层是否可选中
                    if (isSelectableFoldOverlay) {
                        if (iwPolyline.isSelected()) {
                            iwPolyline.setSelected(false);
                            MapElementsHolder.setIdentifyShape(null);
                            EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
                        } else {
                            MapOverlayUtils.clearHighlightFeature(mapViewWeakReference.get());//驱动单选
                            iwPolyline.setSelected(true);
                            MapElementsHolder.setIdentifyShape(iwPolyline);
                            EventBus.getDefault().post(new EventCluster.EventEditGeometry(iwPolyline));
                        }
                        mapViewWeakReference.get().invalidate();
                    }
                }
                return true;
            }
            return false;
        }
    };

    protected static Map<PackageOverlayInfo.OSMGeometryType, Object> clickOverlayListeners = new HashMap();

    static {
        clickOverlayListeners.put(PackageOverlayInfo.OSMGeometryType.POINT, markerClickListener);
        clickOverlayListeners.put(PackageOverlayInfo.OSMGeometryType.LINESTRING, polylineClickListener);
        clickOverlayListeners.put(PackageOverlayInfo.OSMGeometryType.POLYGON, polygonClickListener);
    }

    public static List<PackageOverlay> getEditableOverlays() {
        return editableOverlays;
    }

    public static PackageOverlay getCurrentEditOverlay() {
        return editOverlay;
    }

    public static void setCurrentEditOverlay(PackageOverlay editOverlay) {
        MapElementsHolder.editOverlay = editOverlay;
    }

    public static void setMapView(MapView mapView) {
        mapViewWeakReference = new WeakReference<>(mapView);
    }

    public static MapView getMapView() {
        return mapViewWeakReference.get();
    }

    public static OverlayWithIW getIdentifyShape() {
        return identifyShape;
    }

    public static void setIdentifyShape(OverlayWithIW identifyShape) {
        MapElementsHolder.identifyShape = identifyShape;
    }

}
