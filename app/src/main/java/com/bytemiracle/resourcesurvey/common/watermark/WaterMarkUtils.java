package com.bytemiracle.resourcesurvey.common.watermark;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.watermark.base.BaseWatermarkBizz;
import com.bytemiracle.resourcesurvey.common.watermark.base.WatermarkConfig;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：水印处理类
 * see:
 * 1.针对工程级别的媒体，默认水印为 工程名称（暂不让去除，有需求再说）
 * 2.针对feature级别的媒体，默认水印为 工程名称-图层名-ObjectId (其实也别让去除，其他字段随意)
 * 3.水印不区分图层名称（所有图层使用同一份配置）
 *
 * @author gwwang
 * @date 2021/6/10 9:33
 */
public class WaterMarkUtils {
    /**
     * 初始化工程的水印配置
     *
     * @param dbProject
     */
    // {"工程名称", "图层名称", "OBJECTID", "时间", "中心点", "高程", "颜色"};
    // {"工程名称", "颜色"};
    public static void initDefault(DBProject dbProject) {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        //设置工程的水印配置
        if (TextUtils.isEmpty(dbProject.getProjectMediaWaterMark())) {
            ArrayList<WatermarkConfig.ConfigState> configStates = new ArrayList<>();
            configStates.add(new WatermarkConfig.ConfigState("工程名称", true, null));
            configStates.add(new WatermarkConfig.ConfigState("颜色", true, String.valueOf(Color.YELLOW)));
            dbProject.setProjectMediaWaterMark(JsonParser.toJson(new WatermarkConfig(configStates)));
            dbProjectDao.insertOrReplace(dbProject);
        }
        //设置图层的水印配置
        if (TextUtils.isEmpty(dbProject.getFeatureMediaWaterMark())) {
            ArrayList<WatermarkConfig.ConfigState> configStates = new ArrayList<>();
            configStates.add(new WatermarkConfig.ConfigState("工程名称", true, null));
            configStates.add(new WatermarkConfig.ConfigState("图层名称", true, null));
            configStates.add(new WatermarkConfig.ConfigState("FID", true, null));
            configStates.add(new WatermarkConfig.ConfigState("时间", true, null));
            configStates.add(new WatermarkConfig.ConfigState("颜色", true, String.valueOf(Color.YELLOW)));
            configStates.add(new WatermarkConfig.ConfigState("中心点", false, null));
            configStates.add(new WatermarkConfig.ConfigState("高程", false, null));
            dbProject.setFeatureMediaWaterMark(JsonParser.toJson(new WatermarkConfig(configStates)));
            dbProjectDao.insertOrReplace(dbProject);
        }
    }

    /**
     * 获取所有的配置字段
     *
     * @return
     */
    public static List<String> getWatermarkConfigFields() {
        return matchWatermarkImpl().getWatermarkConfigFields();
    }

    /**
     * 获取水印配置状态
     *
     * @param watermarkName 配置字段值
     * @return
     */
    public static boolean getSetupState(String watermarkName) {
        return matchWatermarkImpl().isConfigSetup(watermarkName);
    }

    /**
     * 获取水印的内容
     *
     * @return
     */
    public static List<String> getWatermarkContentRows() {
        return matchWatermarkImpl().getWatermarkContentRows();
    }

    /**
     * 获取指定的值
     *
     * @param name 可以是配置or已有的字段值
     * @return
     */
    public static String getSpecialValue(String name) {
        return matchWatermarkImpl().getSpecialValue(name);
    }

    /**
     * 设置指定的值
     *
     * @param watermarkName 配置字段值
     * @return
     */
    public static void setNewConfigValue(String watermarkName, boolean newSetupState, String newSetupValue) {
        matchWatermarkImpl().setConfigSetup(watermarkName, newSetupState, newSetupValue);
    }

    /**
     * 是否允许修改默认值
     *
     * @param watermarkName 配置字段值
     * @return
     */
    public static boolean isAllowChangeDefaultSetup(String watermarkName) {
        return matchWatermarkImpl().isAllowChangeState(watermarkName);
    }

    /**
     * 添加水印
     *
     * @param srcBitmap            原始bitmap
     * @param watermarkContentRows 水印内容
     * @return
     */
    public static Bitmap addWatermark(Bitmap srcBitmap, List<String> watermarkContentRows) {
        int TEXT_PADDING = GlobalInstanceHolder.dp5();
        int TEXT_SIZE = GlobalInstanceHolder.sp18();
        int watermarkColor = Integer.parseInt(WaterMarkUtils.getSpecialValue("颜色"));
        if (srcBitmap == null || srcBitmap.getByteCount() == 0) {
            return null;
        }
        Bitmap copy = srcBitmap.copy(srcBitmap.getConfig(), true);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(copy);
        paint.setColor(watermarkColor);
        paint.setTextSize(TEXT_SIZE);

        //获取最长行的内容
        String maxLengthRow = watermarkContentRows.stream()
                .sorted((o1, o2) -> o2.length() - o1.length())
                .collect(Collectors.toList())
                .get(0);

        Rect bounds = new Rect();
        paint.getTextBounds(maxLengthRow, 0, maxLengthRow.length(), bounds);
        int y = copy.getHeight() - (bounds.height() + TEXT_PADDING) * watermarkContentRows.size() - TEXT_PADDING * 10;
        for (String watermarkContent : watermarkContentRows) {
            //绘制水印
            canvas.drawText(watermarkContent, TEXT_PADDING * 10, y, paint);
            y += bounds.height() + TEXT_PADDING;
        }
        if (!srcBitmap.isRecycled())
            srcBitmap.recycle();
        return copy;
    }

    private static BaseWatermarkBizz matchWatermarkImpl() {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        OverlayWithIW identifyShape = MapElementsHolder.getIdentifyShape();
        BaseWatermarkBizz watermarkConfig;
        if (identifyShape != null) {
            //图层
            watermarkConfig = new FeatureWatermarkImpl(openingProject, identifyShape);
        } else {
            //项目
            watermarkConfig = new ProjectWatermarkImpl(openingProject);
        }
        return watermarkConfig;
    }
}
