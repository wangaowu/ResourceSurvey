package com.bytemiracle.resourcesurvey.modules.splash;

import android.util.Log;
import android.widget.ImageView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.component.AbstractSplashActivity;
import com.bytemiracle.base.framework.update.UpdatePresenter;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.xuexiang.xutil.app.ActivityUtils;

import org.greenrobot.greendao.AbstractDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * 类功能：启动闪屏页面
 *
 * @author gwwang
 * @date 2021/3/16 14:21
 */
public class SplashActivity extends AbstractSplashActivity {
    private static final String TAG = "SplashActivity";
    private static final int ALIVE_TIME = 800;
    public static final long VALID_TIME = 100 * 365 * 24 * 60 * 60L;//100年

    //应用内所有的？extends AbstractDao字节码
    //1.1如遇升级业务，请在此注册所有的daoClass
    //1.2框架会创建temp表，作为迁移中介
    public static Class<? extends AbstractDao<?, ?>>[] DAO_CLASSES = new Class[]{
    };

    @Override
    protected void setContentView() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(R.mipmap.splash);
        setContentView(imageView);
    }

    @Override
    protected void doOnResume() {
        String versionName = new UpdatePresenter(this).getVersionName();
        String packageTime = "2022" + versionName.substring(versionName.length() - 4);
        try {
            long packageTimeMillis = new SimpleDateFormat("yyyyMMdd").parse(packageTime).getTime();
            long currentTimeMillis = System.currentTimeMillis();
            if ((currentTimeMillis - packageTimeMillis) * .0001f >= VALID_TIME) {
                //过期
                XToastUtils.error("当前应用不可用!");
                GlobalInstanceHolder.mainHandler().postDelayed(() -> {
                    finish();
                }, ALIVE_TIME * 3);
            } else {
                //未过期
                enterLoginActivity();
            }
        } catch (ParseException e) {
            enterLoginActivity();
        }
    }

    private void enterLoginActivity() {
        GlobalInstanceHolder.mainHandler().postDelayed(() -> {
            ActivityUtils.startActivity(LoginActivity.class);
            finish();
        }, ALIVE_TIME);
    }

    @Override
    protected boolean onUpdateVersionCode() {
        Log.e(TAG, "onUpdateVersionCode(): called");
        //1.代码升级，sp等逻辑
        // 1.1异步逻辑请同步return false，并handler调用doOnResume方法
        // 或者1.2同步逻辑完成请return true就可以
        //FileUtil.clearDirFiles(FileLocalUtils.apkPath);

        // 2.数据库升级
        // 2.1新增在this::DAO_CLASSES内注册
        // 然后2.2在project.build.gradle升级ext.versionCode
        return true;
    }

    @Override
    public void onBackPressed() {
        return;
    }
}

