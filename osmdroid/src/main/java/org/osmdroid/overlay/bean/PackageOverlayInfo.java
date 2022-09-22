package org.osmdroid.overlay.bean;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;

import org.osmdroid.customImpl.geopackage.GeoPackageQuick;

import mil.nga.geopackage.GeoPackage;

/**
 * 类功能：图形的伴随信息对象
 * (PackageOverlay::setPackageLayerInfo(PackageLayerInfo info))
 *
 * @author gwwang
 * @date 2022/2/22 14:07
 */
public class PackageOverlayInfo {
    public enum Category {
        SHP, GEOPACKAGE, GRAPHIC
    }

    public enum OSMGeometryType {
        POINT, LINESTRING, POLYGON
    }

    /**
     * 该图层对应的gpkg文件地址
     */
    private String gpkgPath;
    /**
     * 该图层对应的geopackage对象
     */
    private GeoPackage geoPackage;
    /**
     * 图层是否可选中
     */
    private boolean selectable;
    /**
     * 图层类型
     */
    private Category overlayCategory;
    /**
     * 图形类型
     */
    private OSMGeometryType osmGeometryType;

    public PackageOverlayInfo(String gpkgPath, Category overlayCategory, OSMGeometryType osmGeometryType, boolean selectable) {
        this.gpkgPath = gpkgPath;
        this.overlayCategory = overlayCategory;
        this.osmGeometryType = osmGeometryType;
        this.selectable = selectable;
    }

    /**
     * 获取可编辑的geopackage （需要重新打开）
     *
     * @param writableGeoPackage
     */
    public void openWritableGeoPackage(CommonAsyncListener<GeoPackage> writableGeoPackage) {
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            if (geoPackage != null) {
                geoPackage.close();
            }
            geoPackage = GeoPackageQuick.connectExternalGeoPackage(gpkgPath);
            GlobalInstanceHolder.mainHandler().post(() -> writableGeoPackage.doSomething(geoPackage));
        });
    }

    public OSMGeometryType getOsmGeometryType() {
        return osmGeometryType;
    }

    public void setOsmGeometryType(OSMGeometryType osmGeometryType) {
        this.osmGeometryType = osmGeometryType;
    }

    public Category getOverlayCategory() {
        return overlayCategory;
    }

    public void setOverlayCategory(Category overlayCategory) {
        this.overlayCategory = overlayCategory;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
    }

    @Override
    public String toString() {
        return "PackageOverlayInfo{" +
                "gpkgPath='" + gpkgPath + '\'' +
                ", selectable=" + selectable +
                ", overlayCategory=" + overlayCategory +
                '}';
    }
}
