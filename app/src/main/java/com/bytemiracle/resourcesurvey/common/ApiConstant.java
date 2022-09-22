package com.bytemiracle.resourcesurvey.common;


import com.bytemiracle.resourcesurvey.BuildConfig;

/**
 * 类功能：接口相关常量
 *
 * @author gwwang
 * @date 2021/3/16 14:32
 */
public class ApiConstant {

    //本地调试环境
//    public static final String HOST = "http://192.168.137.1";
//    public static final String PORT = ":8081";
//    public static final boolean DEFAULT_SHOW_VPN = false;

    //配置运行环境
    public static final String HOST = BuildConfig.HOST;
    public static final String PORT = BuildConfig.PORT;

    public static final String SERVER = HOST + PORT;

    public static class HEADER {
        public static final String TOKEN_KEY = "Authorization";
        public static final String DEVICE = "device";
    }
}
