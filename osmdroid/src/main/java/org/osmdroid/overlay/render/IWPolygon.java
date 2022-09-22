package org.osmdroid.overlay.render;

import android.graphics.Canvas;

import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.options.PolygonOptions;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能：可聚焦的polygon
 *
 * @author gwwang
 * @date 2022/2/24 17:20
 */
public class IWPolygon extends Polygon implements ISelectOverlay<PolygonOptions> {

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
    PolygonOptions selectOptions;

    @Override
    public void setSelectOptions(PolygonOptions selectOptions) {
        this.selectOptions = selectOptions;
    }

    public boolean hasHoles() {
        List<List<GeoPoint>> holes = getHoles();
        if (ListUtils.isEmpty(holes)) {
            return false;
        }
        return holes.stream()
                .filter(geoPoints -> !ListUtils.isEmpty(geoPoints))
                .count() > 0;
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
            mFillPaint.setColor(selectOptions.getFillColorOnSelected());
            mOutlinePaint.setColor(selectOptions.getLineColorOnSelected());
            mOutlinePaint.setStrokeWidth(selectOptions.getLineWidthOnSelected());
        } else {
            mFillPaint.setColor(selectOptions.getFillColor());
            mOutlinePaint.setColor(selectOptions.getLineColor());
            mOutlinePaint.setStrokeWidth(selectOptions.getLineWidth());
        }
    }
}
