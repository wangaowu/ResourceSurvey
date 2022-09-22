package com.bytemiracle.resourcesurvey.common.renderstyle;

import org.osmdroid.overlay.bean.OsmRenderStyle;

/**
 * 类功能： 底图渲染配置对象
 *
 * @author gwwang
 * @date 2021/7/26 11:22
 */
public class OverlayRenderStyle extends OsmRenderStyle {

    private String overlayName;
    private String markFieldName;

    public String getMarkFieldName() {
        return markFieldName;
    }

    public void setMarkFieldName(String markFieldName) {
        this.markFieldName = markFieldName;
    }

    public String getOverlayName() {
        return overlayName;
    }

    public void setOverlayName(String overlayName) {
        this.overlayName = overlayName;
    }

    public OverlayRenderStyle(String overlayName, String markFieldName) {
        this(DEFAULT_STYLE, overlayName, markFieldName);
    }

    private OverlayRenderStyle(OsmRenderStyle osmRenderStyle, String overlayName, String markFieldName) {
        setFeatureBoundColor(osmRenderStyle.getFeatureBoundColor());
        setFeatureSolidColor(osmRenderStyle.getFeatureSolidColor());
        setFeatureSolidAlpha(osmRenderStyle.getFeatureSolidAlpha());
        setFeatureBoundLineWidth(osmRenderStyle.getFeatureBoundLineWidth());
        setSelectBoundColor(osmRenderStyle.getSelectBoundColor());
        setSelectSolidColor(osmRenderStyle.getSelectSolidColor());
        setSelectBoundLineWidth(osmRenderStyle.getSelectBoundLineWidth());
        setSelectGrid(osmRenderStyle.getSelectGrid());
        setSelectGridColor(osmRenderStyle.getSelectGridColor());

        setOverlayName(overlayName);
        setMarkFieldName(markFieldName);
    }

    /**
     * 获取默认的渲染样式
     *
     * @param overlayName
     */
    public static OverlayRenderStyle getDefaultStyle(String overlayName) {
        return new OverlayRenderStyle(overlayName, null);
    }
}
