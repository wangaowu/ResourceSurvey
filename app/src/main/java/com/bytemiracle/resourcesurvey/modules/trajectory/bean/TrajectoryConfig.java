package com.bytemiracle.resourcesurvey.modules.trajectory.bean;

import android.graphics.Color;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 类功能：轨迹配置
 *
 * @author gwwang
 * @date 2021/7/16 14:50
 */
public class TrajectoryConfig {
    /**
     * 时间采样率
     */
    public static final int TYPE_BY_TIME = 0;
    /**
     * 距离采样率
     */
    public static final int TYPE_BY_DISTANCE = 1;
    /**
     * 默认轨迹线颜色
     */
    public static final int DEFAULT_COLOR = Color.RED;
    /**
     * 默认轨迹线宽度
     */
    public static final int DEFAULT_LINE_WIDTH = 2;

    //配置采样方式
    public static final LinkedHashMap<String, List<Integer>> SAMPLING_CONFIG = new LinkedHashMap<>();

    //配置采样线条宽度
    public static final String[] LINE_WIDTH_CONFIG = new String[]{"1", "2", "4", "10"};

    static {
        SAMPLING_CONFIG.put("按时间间隔采样", Arrays.asList(1, 2, 3, 5, 10));
        SAMPLING_CONFIG.put("按长度间隔采样", Arrays.asList(1, 3, 5, 10, 50));
    }

    /**
     * 采样线条宽度
     */
    private int lineWidth;
    /**
     * 采样方式
     */
    private int type;
    /**
     * 采样率
     */
    private int samplingRate;
    /**
     * 采样率单位
     */
    private String samplingRateUnit;
    /**
     * 轨迹颜色
     */
    private int color;

    public TrajectoryConfig(int lineWidth, int type, int samplingRate, String samplingRateUnit, int color) {
        this.lineWidth = lineWidth;
        this.type = type;
        this.samplingRate = samplingRate;
        this.samplingRateUnit = samplingRateUnit;
        this.color = color;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    public String getSamplingRateUnit() {
        return samplingRateUnit;
    }

    public void setSamplingRateUnit(String samplingRateUnit) {
        this.samplingRateUnit = samplingRateUnit;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
