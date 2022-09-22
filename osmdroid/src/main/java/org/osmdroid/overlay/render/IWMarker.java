package org.osmdroid.overlay.render;

import android.graphics.Canvas;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.options.MarkerOptions;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能：可聚焦的marker
 *
 * @author gwwang
 * @date 2022/2/24 17:21
 */
public class IWMarker extends Marker implements ISelectOverlay<MarkerOptions> {

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
}
