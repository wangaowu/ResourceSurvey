package org.osmdroid.overlay.converter;

import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.overlay.bean.MultiOverlayWrapper;
import org.osmdroid.overlay.bean.OverlayWrapperType;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

/**
 * 类功能：图层的点击事件分发器
 *
 * @author gwwang
 * @date 2021/12/16 14:05
 */
public class OverlayClickListenerDispatcher {

    private MultiOverlayWrapper overlayWrapper;

    /**
     * 构造方法
     *
     * @param overlayWrapper overlay的包装类
     */
    public OverlayClickListenerDispatcher(MultiOverlayWrapper overlayWrapper) {
        this.overlayWrapper = overlayWrapper;
    }

    /**
     * 设置点击事件
     *
     * @param clickShpListener 点击事件
     */
    public void dispatch(Object clickShpListener) {
        OverlayWrapperType overlayMultiType = overlayWrapper.getOverlayMultiType();
        if (overlayMultiType == OverlayWrapperType.SINGLE) {
            //单图层
            OverlayWithIW overlay = (OverlayWithIW) overlayWrapper.getOverlay();
            dispatchSingle(overlay, clickShpListener);
        } else {
            //多图层
            for (ISelectOverlay overlay : overlayWrapper.getOverlayList()) {
                if (overlay instanceof OverlayWithIW) {
                    dispatchSingle((OverlayWithIW) overlay, clickShpListener);
                }
            }
        }
    }

    private void dispatchSingle(OverlayWithIW overlay, Object clickShpListener) {
        if (overlay instanceof Marker) {
            setMarkerListener((Marker) overlay, clickShpListener);
        } else if (overlay instanceof Polygon) {
            setPolygonListener((Polygon) overlay, clickShpListener);
        } else if (overlay instanceof Polyline) {
            //线条一般没有点击事件
            setPolylineListener((Polyline) overlay, clickShpListener);
        }
    }

    private void setMarkerListener(Marker shape, Object clickShpListener) {
        if (clickShpListener == null) {
            shape.setOnMarkerClickListener((marker, mapView) -> false);
        } else if (clickShpListener instanceof Marker.OnMarkerClickListener) {
            shape.setOnMarkerClickListener((Marker.OnMarkerClickListener) clickShpListener);
        }
    }

    private void setPolylineListener(Polyline overlay, Object clickShpListener) {
        if (clickShpListener == null) {
            overlay.setOnClickListener((polyline, mapView, eventPos) -> false);
        } else if (clickShpListener instanceof Polyline.OnClickListener) {
            overlay.setOnClickListener((Polyline.OnClickListener) clickShpListener);
        }
    }

    private void setPolygonListener(Polygon shape, Object clickShpListener) {
        if (clickShpListener == null) {
            shape.setOnClickListener((polygon1, mapView, eventPos) -> false);
        } else if (clickShpListener instanceof Polygon.OnClickListener) {
            shape.setOnClickListener((Polygon.OnClickListener) clickShpListener);
        }
    }
}
