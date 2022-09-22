package org.osmdroid.overlay.bean.options;

/**
 * 类功能： 选中的配置
 *
 * @author gwwang
 * @date 2022/2/25 10:48
 */
public class BaseSelectOptions {

    /**
     * 填充颜色（polygon）
     */
    protected int fillColor;
    protected int fillColorOnSelected; //选中
    /**
     * 线条颜色和宽度（包括单线条、边界线条）
     */
    protected int lineColor;
    protected float lineWidth;

    protected float lineWidthOnSelected; //选中
    protected int lineColorOnSelected; //选中

    /**
     * 暂且认为是测量时的坐标
     */
    protected boolean geodesic = false;

    public boolean isGeodesic() {
        return geodesic;
    }

    public void setGeodesic(boolean geodesic) {
        this.geodesic = geodesic;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getFillColorOnSelected() {
        return fillColorOnSelected;
    }

    public void setFillColorOnSelected(int fillColorOnSelected) {
        this.fillColorOnSelected = fillColorOnSelected;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getLineWidthOnSelected() {
        return lineWidthOnSelected;
    }

    public void setLineWidthOnSelected(float lineWidthOnSelected) {
        this.lineWidthOnSelected = lineWidthOnSelected;
    }

    public int getLineColorOnSelected() {
        return lineColorOnSelected;
    }

    public void setLineColorOnSelected(int lineColorOnSelected) {
        this.lineColorOnSelected = lineColorOnSelected;
    }
}
