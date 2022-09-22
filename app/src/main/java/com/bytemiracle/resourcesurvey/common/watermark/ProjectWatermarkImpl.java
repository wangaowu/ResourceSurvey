package com.bytemiracle.resourcesurvey.common.watermark;

import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.watermark.base.BaseWatermarkBizz;
import com.bytemiracle.resourcesurvey.common.watermark.base.WatermarkConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：项目水印配置
 *
 * @author gwwang
 * @date 2021/6/10 14:15
 */
public class ProjectWatermarkImpl extends BaseWatermarkBizz {
    private static String[] LOCK_WATERMARKS = {"工程名称", "颜色"};


    public ProjectWatermarkImpl(DBProject project) {
        super(project);
    }

    @Override
    public boolean isAllowChangeState(String watermarkName) {
        return !Arrays.stream(LOCK_WATERMARKS).anyMatch(s -> s.equals(watermarkName));
    }

    @Override
    public List<String> getWatermarkConfigFields() {
        WatermarkConfig watermarkConfig = JsonParser.fromJson(curProject.getProjectMediaWaterMark(), WatermarkConfig.class);
        return watermarkConfig.configStates.stream()
                .map(configState -> configState.watermarkName)
                .collect(Collectors.toList());
    }

    @Override
    public String getSpecialValue(String name) {
        if ("颜色".equals(name)) {
            return super.getConfigValue(name);
        }
        if ("工程名称".equals(name)) {
            return curProject.getName();
        }
        return null;
    }

    @Override
    protected void sinkConfigs2Database(WatermarkConfig newConfigs) {
        DBProject project = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Id.eq(curProject.getId())).unique();
        String newConfigJson = JsonParser.toJson(newConfigs);
        project.setProjectMediaWaterMark(newConfigJson);
        dbProjectDao.insertOrReplace(project);
    }

    @Override
    protected WatermarkConfig getWatermarkConfigOfDatabase() {
        DBProject project = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Id.eq(curProject.getId())).unique();
        return JsonParser.fromJson(project.getProjectMediaWaterMark(), WatermarkConfig.class);
    }
}
