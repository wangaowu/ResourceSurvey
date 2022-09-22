package org.osmdroid.overlay.bean.options;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.bytemiracle.base.framework.GlobalInstanceHolder;

/**
 * 类功能：Polygon 渲染配置类
 *
 * @author gwwang
 * @date 2021/12/16 10:22
 */
public class OsmRenderOption {

    private final MarkerOptions defaultMarkerOption;
    private final PolylineOptions defaultPolylineOption;
    private final PolygonOptions defaultPolygonOption;

    public OsmRenderOption() {
        defaultMarkerOption = initDefaultMarkerOption();
        defaultPolylineOption = initDefaultLineOption();
        defaultPolygonOption = initPolygonOption();
    }

    /**
     * 默认的markerOption
     *
     * @return
     */
    public MarkerOptions getMarkerOption() {
        return defaultMarkerOption;
    }

    /**
     * 默认的线条Option
     *
     * @return
     */
    public PolylineOptions getPolyLineOption() {
        return defaultPolylineOption;
    }

    /**
     * 默认的多边形Option
     *
     * @return
     */
    public PolygonOptions getPolygonOption() {
        return defaultPolygonOption;
    }

    private MarkerOptions initDefaultMarkerOption() {
        int dp5 = GlobalInstanceHolder.dp5();
        GradientDrawable solidDrawable = new GradientDrawable();
        solidDrawable.setShape(GradientDrawable.OVAL);
        solidDrawable.setColor(Color.YELLOW);
        solidDrawable.setSize(dp5, dp5);

        GradientDrawable selectSolidDrawable = new GradientDrawable();
        selectSolidDrawable.setShape(GradientDrawable.OVAL);
        selectSolidDrawable.setColor(Color.YELLOW);
        selectSolidDrawable.setStroke((int) (dp5 * .75f), Color.GREEN);
        selectSolidDrawable.setSize((int) (dp5 * 1.5f), (int) (dp5 * 1.5f));

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon = solidDrawable;
        markerOptions.selectedIcon = selectSolidDrawable;
        markerOptions.title = "";
        markerOptions.alpha = 1.0f;
        markerOptions.text = "";
        return markerOptions;
    }

    private PolylineOptions initDefaultLineOption() {
        PolylineOptions polylineRenderingOptions = new PolylineOptions();
        polylineRenderingOptions.setLineWidth(2f);
        polylineRenderingOptions.setLineColor(0xffCDAA7D);
        polylineRenderingOptions.setLineWidthOnSelected(4f);
        polylineRenderingOptions.setLineColorOnSelected(0xffFFD700);
        return polylineRenderingOptions;
    }

    private PolygonOptions initPolygonOption() {
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.setLineWidth(2f);
        polygonOptions.setLineWidthOnSelected(4f);
        polygonOptions.setLineColor(0xffCDAA7D);
        polygonOptions.setLineColorOnSelected(0xffFFD700);

        polygonOptions.setFillColor(0x80CDAA7D);
        polygonOptions.setFillColorOnSelected(0xA0FFD700);
        return polygonOptions;
    }
}
