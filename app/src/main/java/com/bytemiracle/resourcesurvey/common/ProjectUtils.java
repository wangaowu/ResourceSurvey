package com.bytemiracle.resourcesurvey.common;

import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.file.FileUtils;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;

import java.io.File;
import java.util.List;

/**
 * 类功能：工程
 *
 * @author gwwang
 * @date 2021/5/28 15:00
 */
public class ProjectUtils {
    private static final String TAG = "ProjectUtils";

    /**
     * 创建工程文件系统
     *
     * @param projectName
     */
    public static void createProjectFileSystem(String projectName) {
        //创建工程路径
        String projectPath = FileConstant.getExternalRootDir() + File.separator + "工程列表" + File.separator + projectName;
        FileConstant.ensureDir(projectPath);
        //创建工程内的默认文件夹
        for (String dir : FileConstant.LIST_IN_PROJECT) {
            FileConstant.ensureDir(projectPath + File.separator + dir);
            if (dir.equals("多媒体")) {
                for (String s : FileConstant.LIST_IN_MEDIA) {
                    FileConstant.ensureDir(projectPath + File.separator + dir + File.separator + s);
                }
            }
        }
    }

    /**
     * 删除工程
     *
     * @param dbProject
     */
    public static void deleteProject(DBProject dbProject) {
        GreenDaoManager.getInstance().getDaoSession().getDBProjectDao().delete(dbProject);
        com.xuexiang.xutil.file.FileUtils.deleteDir(getProject(dbProject.getName()));
    }

    /**
     * 切换工程
     *
     * @param projectName
     */
    public static void switchProject(String projectName) {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        List<DBProject> allProjects = dbProjectDao.queryBuilder().list();
        DBProject openProject = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Name.eq(projectName)).unique();
        for (DBProject project : allProjects) {
            if (openProject.getId() == project.getId()) {
                project.setIsLatestProject(1);
            } else {
                project.setIsLatestProject(0);
            }
        }
        dbProjectDao.updateInTx(allProjects);
        GlobalObjectHolder.setOpeningProject(openProject);
    }

    /**
     * 获取工程
     */
    public static File getProject(String projectName) {
        return new File(FileConstant.getExternalRootDir() + File.separator + "工程列表" + File.separator + projectName);
    }

    /**
     * 获取geopackge样板(空文件)
     */
    public static File getSampleGeoPackage() {
        return new File(FileConstant.getExternalRootDir() + File.separator + "默认模板" + File.separator + "empty.gpkg");
    }

    /**
     * 拷贝geopackge样板(空文件)
     */
    public static void ensureSampleGeoPackage() {
        File sampleGeoPackage = getSampleGeoPackage();
        if (!sampleGeoPackage.exists()) {
            GlobalInstanceHolder.newSingleExecutor().execute(() -> {
                try {
                    String parent = sampleGeoPackage.getParentFile().getAbsolutePath();
                    FileUtils.getInstance(GlobalObjectHolder.getMainActivityObject()).copyAssetsFileToSD("empty.gpkg", parent + "/");
                } catch (Exception e) {
                    Log.e(TAG, "ensureSampleGeoPackage: " + e.getMessage());
                }
            });
        }
    }

    /**
     * 获取工程的geopackge文件
     */
    public static String getProjectGeoPackage(String projectName) {
        return getProject(projectName).getAbsolutePath() + File.separator + "采集数据" + File.separator + projectName + ".gpkg";
    }

    /**
     * 获取工程的导出文件目录
     */
    public static String getProjectExportDir(String projectName) {
        return getProject(projectName).getAbsolutePath() + File.separator + "数据导出";
    }

    public static class Media {
        /**
         * 获取工程的多媒体路径
         *
         * @param projectName
         * @return
         */
        public static File getMediaPath(String projectName) {
            String projectPath = getProject(projectName).getAbsolutePath();
            return new File(projectPath + File.separator + "多媒体");
        }
    }
}
