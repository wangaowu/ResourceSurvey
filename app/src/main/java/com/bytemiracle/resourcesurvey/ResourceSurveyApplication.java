package com.bytemiracle.resourcesurvey;

import android.os.Environment;

import com.bytemiracle.base.framework.BaseApplication;
import com.bytemiracle.base.framework.crashLog.CrashHandle;
import com.bytemiracle.base.framework.http.OkGoHttp;
import com.bytemiracle.base.framework.preview.PreviewUtils;
import com.bytemiracle.base.framework.update.UpdateComponent;
import com.bytemiracle.base.framework.utils.encrypt.AesCryptUtil;
import com.bytemiracle.base.framework.utils.sp.EasySharedPreference;
import com.bytemiracle.resourcesurvey.common.ApiConstant;
import com.bytemiracle.resourcesurvey.common.FileConstant;
import com.xuexiang.xui.XUI;
import com.xuexiang.xutil.XUtil;

import org.osmdroid.config.OsmSDKConfig;

import java.util.HashMap;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/3/16 14:29
 */
public class ResourceSurveyApplication extends BaseApplication {
    private static final String TAG = "ResourceSurveyApplication";
    private static final String SP_NAME = "bytemiracle_app";
    /**
     * 自定义aes密钥
     */
    private static final String AES_CLIPPER = "ResourceSurveyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        //okgoHttp
        OkGoHttp.init(this, ApiConstant.SERVER, null, true, new HashMap<>());
        //xuexiangLibs
        initXueXiangLibs();
        //sharedpreference
        EasySharedPreference.init(this, SP_NAME);
        //crash collection
        CrashHandle.getInstance().init(this, FileConstant.getAppDataDir(this), data -> {
            //crash发生时,退出app
            XUtil.get().exitApp();
        });
        //组件的顶部状态栏颜色
        PreviewUtils.initConfig(getColor(R.color.common_dark), true);
        //升级
        UpdateComponent.initResource(R.drawable.ic_launcher, getString(R.string.app_name), FileConstant.getApkPath(this));
        //设置aes密钥
        AesCryptUtil.initCustomAESClipper(AES_CLIPPER);
        //设置osmDroid的缓存路径
        OsmSDKConfig.setCachePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RSurvey/osmdroidCache");
    }

    /**
     * 初始化基础库
     */
    private void initXueXiangLibs() {
        //初始化XUtil工具类
        XUtil.init(this);
        XUtil.debug(false);
        //初始化XUI框架
        XUI.init(this);
        XUI.debug(true);
    }
}
