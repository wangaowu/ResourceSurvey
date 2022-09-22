package com.bytemiracle.resourcesurvey.common.watermark.base;

import java.util.List;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/10 13:55
 */
public class WatermarkConfig {
    public List<ConfigState> configStates;

    public WatermarkConfig(List<ConfigState> configStates) {
        this.configStates = configStates;
    }

    public static class ConfigState {
        public String watermarkName;
        public boolean isSetup;
        public String setupValue;

        public ConfigState(String watermarkName, boolean isSetup, String setupValue) {
            this.watermarkName = watermarkName;
            this.isSetup = isSetup;
            this.setupValue = setupValue;
        }
    }
}
