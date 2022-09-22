package com.bytemiracle.resourcesurvey.common.watermark;

import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.watermark.base.BaseWatermarkBizz;
import com.bytemiracle.resourcesurvey.common.watermark.base.WatermarkConfig;

import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：图层水印配置
 *
 * @author gwwang
 * @date 2021/6/10 14:15
 */
public class FeatureWatermarkImpl extends BaseWatermarkBizz {
    private static String[] LOCK_WATERMARKS = {"工程名称", "图层名称", "FID", "时间", "颜色"};

    private OverlayWithIW identifyOverlay;

    public FeatureWatermarkImpl(DBProject project, OverlayWithIW identifyOverlay) {
        super(project);
        this.identifyOverlay = identifyOverlay;
    }

    @Override
    public boolean isAllowChangeState(String watermarkName) {
        return !Arrays.stream(LOCK_WATERMARKS).anyMatch(s -> s.equals(watermarkName));
    }

    @Override
    public List<String> getWatermarkConfigFields() {
        WatermarkConfig watermarkConfig = JsonParser.fromJson(curProject.getFeatureMediaWaterMark(), WatermarkConfig.class);
        return watermarkConfig.configStates.stream()
                .map(configState -> configState.watermarkName)
                .collect(Collectors.toList());
    }

    @Override
    public String getSpecialValue(String name) {
        if ("颜色".equals(name)) {
            return super.getConfigValue(name);
        }
        switch (name) {
            case "工程名称":
                return curProject.getName();
            case "图层名称":
                return ((ISelectOverlay) identifyOverlay).getFeatureOverlayInfo().getPackageOverlay().getName();
            case "FID":
                FeatureRow featureRow = ((ISelectOverlay) identifyOverlay).getFeatureOverlayInfo().getFeatureRow();
                return String.valueOf(featureRow.getId());
            case "时间":
                return String.valueOf(System.currentTimeMillis());
            case "中心点":
                return identifyOverlay.getBounds().getCenterWithDateLine().toString();
            case "高程":
                return String.valueOf(0);
        }
        return "";
    }

    @Override
    protected void sinkConfigs2Database(WatermarkConfig newConfigs) {
        DBProject project = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Id.eq(curProject.getId())).unique();
        String newConfigJson = JsonParser.toJson(newConfigs);
        project.setFeatureMediaWaterMark(newConfigJson);
        dbProjectDao.insertOrReplace(project);
    }

    @Override
    protected WatermarkConfig getWatermarkConfigOfDatabase() {
        DBProject project = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Id.eq(curProject.getId())).unique();
        return JsonParser.fromJson(project.getFeatureMediaWaterMark(), WatermarkConfig.class);
    }
}
