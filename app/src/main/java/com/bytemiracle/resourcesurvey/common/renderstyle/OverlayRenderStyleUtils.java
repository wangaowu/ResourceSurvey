package com.bytemiracle.resourcesurvey.common.renderstyle;

import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;

import org.osmdroid.overlay.bean.OsmRenderStyle;
import org.osmdroid.overlay.bean.options.OsmRenderOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 类功能： 底图渲染配置工具类
 *
 * @author gwwang
 * @date 2021/7/26 11:22
 */
public class OverlayRenderStyleUtils {

    /**
     * 插入图层渲染配置
     *
     * @param overlayName
     */
    public static void insertOverlayConfig(String overlayName) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();

        List<OverlayRenderStyle> otherOverlayRenderStyles;
        String renderStyleConfig = openingProject.getRenderStyleConfig();
        if (TextUtils.isEmpty(renderStyleConfig)) {
            otherOverlayRenderStyles = new ArrayList<>();
        } else {
            otherOverlayRenderStyles = Arrays.asList(JsonParser.fromJson(renderStyleConfig, OverlayRenderStyle[].class));
        }

        OverlayRenderStyle defaultStyle = OverlayRenderStyle.getDefaultStyle(overlayName);

        List<OverlayRenderStyle> newOverlayRenderStyles = new ArrayList<>(otherOverlayRenderStyles);
        newOverlayRenderStyles.add(defaultStyle);

        openingProject.setRenderStyleConfig(JsonParser.toJson(newOverlayRenderStyles));
        GlobalObjectHolder.setOpeningProject(openingProject);
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        dbProjectDao.update(openingProject);
    }

    /**
     * 更新渲染样式
     *
     * @param overlayName
     * @param overlayRenderStyle
     */
    public static void updateOverlayRenderStyle(String overlayName, OverlayRenderStyle overlayRenderStyle) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();

        String renderStyleConfig = openingProject.getRenderStyleConfig();
        List<OverlayRenderStyle> overlayRenderStyles = Arrays.asList(JsonParser.fromJson(renderStyleConfig, OverlayRenderStyle[].class));
        List<OverlayRenderStyle> otherOverlayStyles = overlayRenderStyles.stream()
                .filter(overlayRenderStyle1 -> !overlayRenderStyle1.getOverlayName().equalsIgnoreCase(overlayName))
                .collect(Collectors.toList());

        ArrayList<OverlayRenderStyle> newOverlayRenderStyles = new ArrayList<>(otherOverlayStyles);
        newOverlayRenderStyles.add(overlayRenderStyle);
        openingProject.setRenderStyleConfig(JsonParser.toJson(newOverlayRenderStyles));
        GlobalObjectHolder.setOpeningProject(openingProject);
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        dbProjectDao.update(openingProject);
    }

    /**
     * 删除渲染样式
     *
     * @param overlayName
     */
    public static void deleteOverlayRenderStyle(String overlayName) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();

        String renderStyleConfig = openingProject.getRenderStyleConfig();
        List<OverlayRenderStyle> overlayRenderStyles = Arrays.asList(JsonParser.fromJson(renderStyleConfig, OverlayRenderStyle[].class));
        List<OverlayRenderStyle> otherOverlayStyles = overlayRenderStyles.stream()
                .filter(overlayRenderStyle1 -> !overlayRenderStyle1.getOverlayName().equalsIgnoreCase(overlayName))
                .collect(Collectors.toList());

        openingProject.setRenderStyleConfig(JsonParser.toJson(otherOverlayStyles));
        GlobalObjectHolder.setOpeningProject(openingProject);
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        dbProjectDao.update(openingProject);
    }


    /**
     * 获取图层的overlayStyle
     *
     * @param overlayName
     */
    public static OverlayRenderStyle getOverlayStyle(String overlayName) {
        String renderStyleConfig = GlobalObjectHolder.getOpeningProject().getRenderStyleConfig();
        OverlayRenderStyle[] overlayRenderStyles = JsonParser.fromJson(renderStyleConfig, OverlayRenderStyle[].class);
        Optional<OverlayRenderStyle> renderStyle = Arrays.stream(overlayRenderStyles)
                .filter(overlayRenderStyle -> overlayRenderStyle.getOverlayName().equalsIgnoreCase(overlayName))
                .findFirst();
        if (renderStyle.isPresent()) {
            return renderStyle.get();
        } else {
            return new OverlayRenderStyle(overlayName, null);
        }
    }

    /**
     * 获取当前图层的渲染配置
     *
     * @param overlayName 图层名称
     */
    public static OsmRenderOption getConfigRenderOption(String overlayName) {
        OverlayRenderStyle overlayStyle = getOverlayStyle(overlayName);
        return convertRenderStyle(overlayStyle);
    }

    /**
     * 获取当前图层的渲染配置
     *
     * @param overlayStyle 图层配置
     */
    public static OsmRenderOption getConfigRenderOption(OverlayRenderStyle overlayStyle) {
        return convertRenderStyle(overlayStyle);
    }

    /**
     * 获取默认图层的渲染配置
     * 目前适用于:
     * 1.轨迹绘制
     */
    public static OsmRenderOption getDefaultRenderOption() {
        return convertRenderStyle(OsmRenderStyle.DEFAULT_STYLE);
    }

    private static OsmRenderOption convertRenderStyle(OsmRenderStyle osmRenderStyle) {
        OsmRenderOption osmRenderOption = new OsmRenderOption();
        if (osmRenderStyle != null) {
            int baseSize = GlobalInstanceHolder.dp5();//5dp
            //1.线条配置
            //留意此处，动态构建圆点样式，尺寸与线条宽度相关,颜色为填充颜色
            osmRenderOption.getPolyLineOption().setLineWidth(osmRenderStyle.getFeatureBoundLineWidth());
            osmRenderOption.getPolyLineOption().setLineColor(osmRenderStyle.getFeatureSolidColor());
            osmRenderOption.getPolyLineOption().setLineWidthOnSelected(osmRenderStyle.getSelectBoundLineWidth());
            osmRenderOption.getPolyLineOption().setLineColorOnSelected(osmRenderStyle.getSelectSolidColor());
            //2.点配置
            //留意此处，动态构建圆点样式Drawable，尺寸与线条宽度比例相关,颜色为填充颜色
            //2.1 未选中样式
            int dotSize = baseSize * osmRenderStyle.getFeatureBoundLineWidth();
            GradientDrawable featureSolidDrawable = new GradientDrawable();
            featureSolidDrawable.setShape(GradientDrawable.OVAL);
            featureSolidDrawable.setColor(osmRenderStyle.getFeatureSolidColor());
            featureSolidDrawable.setSize(dotSize, dotSize);
            //2.2 选中样式
            int selectedDotSize = baseSize * osmRenderStyle.getSelectBoundLineWidth();
            GradientDrawable selectSolidDrawable = new GradientDrawable();
            selectSolidDrawable.setShape(GradientDrawable.OVAL);
            selectSolidDrawable.setColor(osmRenderStyle.getFeatureSolidColor());
            selectSolidDrawable.setStroke(baseSize / 2, osmRenderStyle.getSelectSolidColor());
            selectSolidDrawable.setSize(selectedDotSize, selectedDotSize);
            //2.3 设置样式
            osmRenderOption.getMarkerOption().setIcon(featureSolidDrawable);
            osmRenderOption.getMarkerOption().setSelectedIcon(selectSolidDrawable);
            //3.多边形配置
            osmRenderOption.getPolygonOption().setLineWidth(osmRenderStyle.getFeatureBoundLineWidth());
            osmRenderOption.getPolygonOption().setLineColor(osmRenderStyle.getFeatureBoundColor());
            osmRenderOption.getPolygonOption().setLineWidthOnSelected(osmRenderStyle.getSelectBoundLineWidth());
            osmRenderOption.getPolygonOption().setLineColorOnSelected(osmRenderStyle.getSelectBoundColor());
            osmRenderOption.getPolygonOption().setFillColor(osmRenderStyle.getFeatureSolidColor());
            osmRenderOption.getPolygonOption().setFillColorOnSelected(osmRenderStyle.getSelectSolidColor());
        }
        return osmRenderOption;
    }
}
