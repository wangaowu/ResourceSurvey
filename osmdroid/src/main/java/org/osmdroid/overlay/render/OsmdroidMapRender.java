package org.osmdroid.overlay.render;

import android.text.TextUtils;

import org.osmdroid.customImpl.geopackage.GpkgGeometryConverter;
import org.osmdroid.overlay.bean.MultiOverlayWrapper;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.bean.options.OsmRenderOption;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.Geometry;

/**
 * 类功能：处理osmdroid地图的覆盖物
 *
 * @author gwwang
 * @date 2022/2/17 14:49
 */
public class OsmdroidMapRender {

    private WeakReference<MapView> mapView;
    private PackageOverlay folderOverlay;
    private GpkgGeometryConverter osmShapeConverter;

    /**
     * 构造方法
     *
     * @param mapView      osmdroid mapView object
     * @param renderOption 渲染配置
     * @param overlayName  overlay的名称
     */
    public OsmdroidMapRender(MapView mapView, OsmRenderOption renderOption, String overlayName, PackageOverlayInfo.Category category, PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        this(mapView, renderOption, createPackageOverlay(overlayName, null, category, osmGeometryType), true);
    }

    /**
     * 构造方法
     *
     * @param mapView      osmdroid mapView object
     * @param renderOption 渲染配置
     * @param overlayName  overlay的名称
     */
    public OsmdroidMapRender(MapView mapView, OsmRenderOption renderOption, String overlayName, String gpkgPath, PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        this(mapView, renderOption, overlayName, gpkgPath, PackageOverlayInfo.Category.GEOPACKAGE, osmGeometryType);
    }

    /**
     * 构造方法
     *
     * @param mapView      osmdroid mapView object
     * @param renderOption 渲染配置
     * @param overlayName  overlay的名称
     */
    public OsmdroidMapRender(MapView mapView, OsmRenderOption renderOption, String overlayName, String gpkgPath, PackageOverlayInfo.Category category, PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        this(mapView, renderOption, assertOnlyOverlay(mapView, overlayName, gpkgPath, category, osmGeometryType), false);
    }

    /**
     * 构造方法
     *
     * @param mapView       osmdroid mapView object
     * @param renderOption  渲染配置
     * @param folderOverlay 指定的overlay
     */
    public OsmdroidMapRender(MapView mapView, OsmRenderOption renderOption, PackageOverlay folderOverlay) {
        this(mapView, renderOption, folderOverlay, true);
    }

    /**
     * 构造方法
     *
     * @param mapView       osmdroid mapView object
     * @param renderOption  渲染配置
     * @param folderOverlay 指定的overlay
     * @param checkSingle   使用name检查唯一图层
     */
    public OsmdroidMapRender(MapView mapView, OsmRenderOption renderOption, PackageOverlay folderOverlay, boolean checkSingle) {
        this.mapView = new WeakReference<>(mapView);
        if (checkSingle) {
            this.folderOverlay = assertOnlyOverlay(mapView, folderOverlay.getName(), null, PackageOverlayInfo.Category.GRAPHIC, folderOverlay.getPackageOverlayInfo().getOsmGeometryType());
        } else {
            this.folderOverlay = folderOverlay;
        }
        this.osmShapeConverter = new GpkgGeometryConverter(renderOption, mapView);
    }

    /**
     * 获取当前渲染的图层
     *
     * @return
     */
    public PackageOverlay getPackageOverlay() {
        return folderOverlay;
    }

    /**
     * 移除自身overlay的所有要素
     */
    public void removeItems() {
        folderOverlay.getItems().clear();
    }

    /**
     * 移除自身的overlay
     */
    public void removeSelfOverlay() {
        removeItems();
        mapView.get().getOverlayManager().overlays().remove(folderOverlay);
        mapView.get().invalidate();
    }

    /**
     * 添加gpkg的feature行
     *
     * @param mapView
     * @param featureRow    geopackage::featureRow
     * @param markFieldName
     * @return
     */
    public MultiOverlayWrapper addOverlayUseFeature(MapView mapView, FeatureRow featureRow, String markFieldName) {
        Geometry geometry = featureRow.getGeometry().getGeometry();
        MultiOverlayWrapper overlayWrapper = osmShapeConverter.fromGpkgGeometry(geometry);
        overlayWrapper.setFeatureOverlayInfo(folderOverlay, featureRow);
        addOverlay(overlayWrapper);

        //添加标注
        if (geometry.getGeometryType().getName().contains("POLYGON") && !TextUtils.isEmpty(markFieldName)) {
            Object markerValue = featureRow.getValue(markFieldName);
            if (markerValue != null && !"".equals(markerValue)) {
                addOverlay(osmShapeConverter.fromGpkgMark(mapView, geometry.getCentroid(), markerValue.toString()));
            }
        }
        return overlayWrapper;
    }

    /**
     * 添加geometry的包装对象
     *
     * @param overlayWrapper
     */
    public void addOverlay(MultiOverlayWrapper overlayWrapper) {
        switch (overlayWrapper.getOverlayMultiType()) {
            case SINGLE:
                addSelectableOverlay(overlayWrapper.getOverlay());
                break;
            case MULTIPLE:
                addSelectableOverlay(overlayWrapper.getOverlayList());
                break;
        }
    }

    /**
     * 添加geometry
     *
     * @param shapes 可选中的shape列表
     */
    public void addSelectableOverlay(List<ISelectOverlay> shapes) {
        for (ISelectOverlay shape : shapes) {
            if (shape instanceof OverlayWithIW) {
                addOverlay((OverlayWithIW) shape, false);
            }
        }
        mapView.get().invalidate();
    }

    /**
     * 添加geometry
     *
     * @param shape 可选中的shape图形
     */
    public void addSelectableOverlay(ISelectOverlay shape) {
        if (shape instanceof OverlayWithIW) {
            addOverlay((OverlayWithIW) shape, true);
        }
    }

    /**
     * 添加geometry
     *
     * @param shapes shape列表
     */
    public void addOverlay(List<OverlayWithIW> shapes) {
        for (OverlayWithIW shape : shapes) {
            addOverlay(shape, false);
        }
        mapView.get().invalidate();
    }

    /**
     * 添加geometry
     *
     * @param shape shape图形
     */
    public void addOverlay(OverlayWithIW shape) {
        addOverlay(shape, true);
    }

    /**
     * 添加geometry
     *
     * @param shape          shape图形
     * @param callInvalidate 是否立即刷新
     */
    public void addOverlay(OverlayWithIW shape, boolean callInvalidate) {
        folderOverlay.add(shape);
        if (callInvalidate) {
            mapView.get().invalidate();
        }
    }

    private static PackageOverlay assertOnlyOverlay(MapView mapView, String overlayName, String gpkgPath, PackageOverlayInfo.Category overlayCategory, PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        //查找已经添加的overlay
        Optional<PackageOverlay> existOverlay = mapView.getOverlayManager().overlays()
                .stream().filter(overlay -> {
                    if (overlay instanceof PackageOverlay) {
                        return ((PackageOverlay) overlay).getName().equals(overlayName);
                    }
                    return false;
                })
                .map(overlay -> (PackageOverlay) overlay).findFirst();
        if (existOverlay.isPresent()) {
            return existOverlay.get();
        }
        //新增overlay
        PackageOverlay folderOverlay = createPackageOverlay(overlayName, gpkgPath, overlayCategory, osmGeometryType);
        mapView.getOverlayManager().add(folderOverlay);
        return folderOverlay;
    }

    private static PackageOverlay createPackageOverlay(String overlayName, String gpkgPath, PackageOverlayInfo.Category overlayCategory, PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        PackageOverlay folderOverlay = new PackageOverlay();
        folderOverlay.setName(overlayName);
        folderOverlay.setPackageOverlayInfo(new PackageOverlayInfo(gpkgPath, overlayCategory, osmGeometryType, false));
        return folderOverlay;
    }
}
