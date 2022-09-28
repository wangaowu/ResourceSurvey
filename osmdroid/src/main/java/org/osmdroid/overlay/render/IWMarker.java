package org.osmdroid.overlay.render;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.options.MarkerOptions;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 类功能：可聚焦的marker
 *
 * @author gwwang
 * @date 2022/2/24 17:21
 */
public class IWMarker extends Marker implements ISelectOverlay<MarkerOptions> {
    private static final int TOLERANCE = 12;

    /**
     * 为了实现点的点击容差，重写此方法
     *
     * @param event
     * @param mapView
     * @return
     */
    @Override
    public boolean hitTest(final MotionEvent event, final MapView mapView) {
        try {
            boolean displayed = (boolean) getSuperField("mDisplayed");
            Rect orientedMarkerRect = (Rect) getSuperField("mOrientedMarkerRect");

            //拷贝矩形，并向外扩充容差范围
            RectF rectF = new RectF(orientedMarkerRect);
            rectF.inset(-TOLERANCE, -TOLERANCE);

            return mIcon != null && displayed && rectF.contains((int) event.getX(), (int) event.getY());
        } catch (Exception e) {
            return super.hitTest(event, mapView);
        }
    }

    /**
     * 绑定对象
     */
    FeatureOverlayInfo featureOverlayInfo = new FeatureOverlayInfo(null, null);
    /**
     * 是否选中
     */
    boolean selected = false;
    /**
     * 关联的图层集合
     */
    List<ISelectOverlay> composeOverlays = new ArrayList<>();
    /**
     * 选中样式的配置
     */
    private MarkerOptions selectOptions;

    public IWMarker(MapView mapView) {
        super(mapView);
    }

    @Override
    public void setSelected(boolean selected_) {
        selected = selected_;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public FeatureOverlayInfo getFeatureOverlayInfo() {
        return featureOverlayInfo;
    }

    @Override
    public void setFeatureOverlayInfo(FeatureOverlayInfo featureOverlayInfo_) {
        featureOverlayInfo = featureOverlayInfo_;
    }

    @Override
    public List<ISelectOverlay> getComposeOverlays() {
        return composeOverlays;
    }

    @Override
    public void setComposeOverlays(List<ISelectOverlay> composeOverlays_) {
        composeOverlays = composeOverlays_;
    }

    @Override
    public void setSelectOptions(@NotNull MarkerOptions makerOptions) {
        this.selectOptions = makerOptions;
    }

    @Override
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        configPaintBySelect();
        super.draw(pCanvas, pProjection);
    }

    private void configPaintBySelect() {
        if (selectOptions != null) {
            setIcon(selected ? selectOptions.getSelectedIcon() : selectOptions.getIcon());
        }
    }

    //简单一个获取父类private value的工具方法
    private Object getSuperField(String paramString) throws Exception {
        Field field = Objects.requireNonNull(IWMarker.class.getSuperclass()).getDeclaredField(paramString);
        field.setAccessible(true);
        return field.get(this);
    }
}
