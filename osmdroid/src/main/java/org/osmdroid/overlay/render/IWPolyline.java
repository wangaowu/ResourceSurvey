package org.osmdroid.overlay.render;

import android.graphics.Canvas;

import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.options.PolylineOptions;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能：可聚焦的polyline
 *
 * @author gwwang
 * @date 2022/2/24 17:20
 */
public class IWPolyline extends Polyline implements ISelectOverlay<PolylineOptions> {

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
     * 选中的配置
     */
    PolylineOptions selectOptions;

    @Override
    public void setSelectOptions(PolylineOptions selectOptions) {
        this.selectOptions = selectOptions;
    }

    @Override
    public void setSelected(boolean selected_) {
        selected = selected_;
    }

    @Override
    public boolean isSelected() {
        return selected;
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
    public void draw(final Canvas pCanvas, final Projection pProjection) {
        configPaintBySelect();
        super.draw(pCanvas, pProjection);
    }

    private void configPaintBySelect() {
        if (selectOptions == null) {
            return;
        }
        if (selected) {
            mOutlinePaint.setColor(selectOptions.getLineColorOnSelected());
            mOutlinePaint.setStrokeWidth(selectOptions.getLineWidthOnSelected());
        } else {
            mOutlinePaint.setColor(selectOptions.getLineColor());
            mOutlinePaint.setStrokeWidth(selectOptions.getLineWidth());
        }
    }
}
