package com.bytemiracle.resourcesurvey.common.watermark.base;

import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/10 14:15
 */
public abstract class BaseWatermarkBizz {

    protected DBProjectDao dbProjectDao;
    protected DBProject curProject;

    private WatermarkConfig watermarkConfig;

    public BaseWatermarkBizz(DBProject curProject) {
        this.dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        this.curProject = curProject;
        this.watermarkConfig = getWatermarkConfigOfDatabase();
    }

    /**
     * 是否打开
     *
     * @param watermarkName
     * @return
     */
    public boolean isConfigSetup(String watermarkName) {
        return matchConfig(watermarkName).isSetup;
    }

    /**
     * 保存打开状态
     *
     * @param watermarkName
     * @return
     */
    public void setConfigSetup(String watermarkName, boolean setupState, String setupValue) {
        WatermarkConfig.ConfigState configState = matchConfig(watermarkName);
        configState.isSetup = setupState;
        configState.setupValue = setupValue;
        sinkConfigs2Database(watermarkConfig);
        this.watermarkConfig = getWatermarkConfigOfDatabase();
    }

    /**
     * 获取配置的值
     *
     * @param watermarkName
     * @return
     */
    public String getConfigValue(String watermarkName) {
        if (!isConfigSetup(watermarkName)) {
            return null;
        }
        WatermarkConfig.ConfigState configState = matchConfig(watermarkName);
        return configState.setupValue;
    }

    /**
     * 设置配置的值
     *
     * @param watermarkName
     * @param configValue
     */
    public void getConfigValue(String watermarkName, String configValue) {
        WatermarkConfig.ConfigState configState = matchConfig(watermarkName);
        configState.setupValue = configValue;
        sinkConfigs2Database(watermarkConfig);
        this.watermarkConfig = getWatermarkConfigOfDatabase();
    }

    /**
     * 是否允许改变状态
     *
     * @param watermarkName
     * @return
     */
    public abstract boolean isAllowChangeState(String watermarkName);

    /**
     * 获取所有的水印配置项
     *
     * @return
     */
    public abstract List<String> getWatermarkConfigFields();

    /**
     * 获取指定值
     *
     * @param name
     * @return
     */
    public abstract String getSpecialValue(String name);

    /**
     * 获取水印的内容
     *
     * @return
     */
    public List<String> getWatermarkContentRows() {
        List<String> contentRows = new ArrayList<>();
        List<String> watermarkNames = getWatermarkConfigFields().stream()
                .filter(name -> !"颜色".equals(name) && isConfigSetup(name))
                .collect(Collectors.toList());

        for (String watermarkName : watermarkNames) {
            if ("时间".equals(watermarkName)) {
                contentRows.add(watermarkName + ":" + AppTime.formatDateTime(new Date(Long.parseLong(getSpecialValue(watermarkName)))));
            } else {
                contentRows.add(watermarkName + ":" + getSpecialValue(watermarkName));
            }
        }
        return contentRows;
    }

    private WatermarkConfig.ConfigState matchConfig(String watermarkName) {
        for (WatermarkConfig.ConfigState configState : this.watermarkConfig.configStates) {
            if (watermarkName.equals(configState.watermarkName)) {
                return configState;
            }
        }
        return null;
    }

    protected abstract void sinkConfigs2Database(WatermarkConfig newConfigs);

    protected abstract WatermarkConfig getWatermarkConfigOfDatabase();
}
