package com.bytemiracle.resourcesurvey.common.global;

import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.modules.main.MainActivity;

import java.lang.ref.WeakReference;

/**
 * 类功能：全局引用持有类
 *
 * @author gwwang
 * @date 2021/5/25 10:38
 */
public class GlobalObjectHolder {
    private static WeakReference<MainActivity> mainActivityWeakReference;
    private static DBProject openingProject;

    public static void setMainActivityObject(MainActivity mainActivityObject) {
        mainActivityWeakReference = new WeakReference(mainActivityObject);
    }

    public static MainActivity getMainActivityObject() {
        return mainActivityWeakReference.get();
    }

    public static DBProject getOpeningProject() {
        return openingProject;
    }

    public static void setOpeningProject(DBProject openingProject) {
        GlobalObjectHolder.openingProject = openingProject;
    }


}
