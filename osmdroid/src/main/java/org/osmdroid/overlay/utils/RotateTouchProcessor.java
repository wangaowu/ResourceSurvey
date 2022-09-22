package org.osmdroid.overlay.utils;

import org.osmdroid.views.MapView;

import java.lang.ref.WeakReference;

/**
 * 类功能：禁止地图旋转的touchListener
 *
 * @author gwwang
 * @date 2021/6/17 8:44
 */
public class RotateTouchProcessor {

    private WeakReference<MapView> mapView;

    public RotateTouchProcessor(MapView mapView) {
        this.mapView = new WeakReference(mapView);
    }

    /**
     * 禁用旋转
     */
    public void disableRotate() {
        MapOverlayUtils.ensureRotationGestureOverlay(mapView.get()).setEnabled(false);
    }

    /**
     * 启用旋转
     */
    public void enableRotate() {
        MapOverlayUtils.ensureRotationGestureOverlay(mapView.get()).setEnabled(true);
    }
}
