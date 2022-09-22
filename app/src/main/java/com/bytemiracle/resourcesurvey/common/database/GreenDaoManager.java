package com.bytemiracle.resourcesurvey.common.database;

import android.content.Context;
import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.file.FileUtil;
import com.bytemiracle.resourcesurvey.cache_greendao.DaoMaster;
import com.bytemiracle.resourcesurvey.cache_greendao.DaoSession;
import com.bytemiracle.resourcesurvey.modules.splash.SplashActivity;
import com.github.yuweiguocn.library.greendao.MigrationHelper;

import org.greenrobot.greendao.database.Database;

import java.io.File;

/**
 * 数据库管理器
 */
public class GreenDaoManager {
    private DaoSession daoSession;
    private boolean isInited;

    private static final GreenDaoManager sInstance = new GreenDaoManager();

    public static GreenDaoManager getInstance() {
        return sInstance;
    }

    private GreenDaoManager() {
    }

    /**
     * 初始化DaoSession
     *
     * @param dbPath 数据库路径
     */
    public void init(String dbPath) {
        File file = new File(dbPath);
        //判断文件是否存在，不存在则创建父级目录
        if (!file.exists()) {
            FileUtil.checkDir(file.getParent());
        }
        if (!isInited) {
            DaoMaster.OpenHelper openHelper = new CustomSQLiteOpenHelper(GlobalInstanceHolder.applicationContext(), dbPath);
            DaoMaster daoMaster = new DaoMaster(openHelper.getWritableDatabase());
            daoSession = daoMaster.newSession();
            isInited = true;
        }
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    /**
     * 数据库升级业务
     */
    private static class CustomSQLiteOpenHelper extends DaoMaster.DevOpenHelper {
        private static final String TAG = "UpgradeSQLiteOpenHelper";

        public CustomSQLiteOpenHelper(Context context, String name) {
            super(context, name, null);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Log.d(TAG, "数据库升级开始，onUpgrade: oldVersion=" + oldVersion + " , newVersion=" + newVersion);
            MigrationHelper.migrate(db, SplashActivity.DAO_CLASSES);
            Log.d(TAG, "数据库升级完毕，onUpgrade: oldVersion=" + oldVersion + " , newVersion=" + newVersion);
//          do custom sql upgrade on every version
//          if (newVersion == oldVersion) {
//              super.onUpgrade(db, oldVersion, newVersion);
//          }
        }
    }
}
