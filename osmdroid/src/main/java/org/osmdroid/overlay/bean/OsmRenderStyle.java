package org.osmdroid.overlay.bean;

import android.graphics.Color;

/**
 * 类功能： 渲染配置样式
 *
 * @author gwwang
 * @date 2021/7/26 11:22
 */
public class OsmRenderStyle {
    private static final String TAG = "OsmRenderStyle";
    /**
     * osmDroidMapView 默认的渲染设置
     */
    public static final OsmRenderStyle DEFAULT_STYLE = new OsmRenderStyle(
            0x80CDAA7D, 0xff333333, 1.0d, 2,
            0xA0FFD700, 0xffFFD700, 4,
            true, Color.GREEN);

    /**
     * 透明化颜色
     *
     * @param color
     * @param alpha
     * @return
     */
    public static int makeAlpha(int color, double alpha) {
        //kotlin's code like this:
        //val alpha = 0x70000000
        //polygon.fillPaint.color = ((polygon.fillPaint.color shl 8)shr 8)or alpha
        long bitNoAlpha = color ^ 0xff000000;
        String bitAlpha = Long.toHexString((int) (alpha * 255));
        return (int) (bitNoAlpha | Long.parseLong(bitAlpha + "000000", 16));
    }

    private int featureSolidColor;

    private int featureBoundColor;

    private double featureSolidAlpha;

    private int featureBoundLineWidth;

    private int selectSolidColor;

    private int selectBoundColor;

    private int selectBoundLineWidth;

    private boolean selectGrid;

    private int selectGridColor;

    public OsmRenderStyle(int featureSolidColor, int featureBoundColor, double featureSolidAlpha,
                          int featureBoundLineWidth, int selectSolidColor, int selectBoundColor,
                          int selectBoundLineWidth, boolean selectGrid, int selectGridColor) {
        this.featureSolidColor = featureSolidColor;
        this.featureBoundColor = featureBoundColor;
        this.featureSolidAlpha = featureSolidAlpha;
        this.featureBoundLineWidth = featureBoundLineWidth;
        this.selectSolidColor = selectSolidColor;
        this.selectBoundColor = selectBoundColor;
        this.selectBoundLineWidth = selectBoundLineWidth;
        this.selectGrid = selectGrid;
        this.selectGridColor = selectGridColor;
    }

    public OsmRenderStyle() {
    }

    public int getFeatureSolidColor() {//透明化
        return makeAlpha(this.featureSolidColor, this.featureSolidAlpha);
    }

    public int getSelectSolidColor() {//透明化
        return makeAlpha(this.selectSolidColor, this.featureSolidAlpha);
    }

    public double getFeatureSolidAlpha() {
        return featureSolidAlpha;
    }

    public void setFeatureSolidAlpha(double featureSolidAlpha) {
        this.featureSolidAlpha = featureSolidAlpha;
    }

    public boolean isSelectGrid() {
        return selectGrid;
    }

    public void setFeatureSolidColor(int featureSolidColor) {
        this.featureSolidColor = featureSolidColor;
    }

    public int getFeatureBoundColor() {
        return this.featureBoundColor;
    }

    public void setFeatureBoundColor(int featureBoundColor) {
        this.featureBoundColor = featureBoundColor;
    }

    public int getFeatureBoundLineWidth() {
        return this.featureBoundLineWidth;
    }

    public void setFeatureBoundLineWidth(int featureBoundLineWidth) {
        this.featureBoundLineWidth = featureBoundLineWidth;
    }

    public void setSelectSolidColor(int selectSolidColor) {
        this.selectSolidColor = selectSolidColor;
    }

    public int getSelectBoundColor() {
        return this.selectBoundColor;
    }

    public void setSelectBoundColor(int selectBoundColor) {
        this.selectBoundColor = selectBoundColor;
    }

    public int getSelectBoundLineWidth() {
        return this.selectBoundLineWidth;
    }

    public void setSelectBoundLineWidth(int selectBoundLineWidth) {
        this.selectBoundLineWidth = selectBoundLineWidth;
    }

    public boolean getSelectGrid() {
        return this.selectGrid;
    }

    public void setSelectGrid(boolean selectGrid) {
        this.selectGrid = selectGrid;
    }

    public int getSelectGridColor() {
        return this.selectGridColor;
    }

    public void setSelectGridColor(int selectGridColor) {
        this.selectGridColor = selectGridColor;
    }
}
