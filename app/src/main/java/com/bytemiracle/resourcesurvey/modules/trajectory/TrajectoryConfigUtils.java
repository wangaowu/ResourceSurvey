package com.bytemiracle.resourcesurvey.modules.trajectory;

import android.text.TextUtils;

import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryConfig;

/**
 * 类功能：水印处理类
 * see:
 * 1.针对工程级别的媒体，默认水印为 工程名称（暂不让去除，有需求再说）
 * 2.针对feature级别的媒体，默认水印为 工程名称-图层名-ObjectId (其实也别让去除，其他字段随意)
 * 3.水印不区分图层名称（所有图层使用同一份配置）
 *
 * @author gwwang
 * @date 2021/6/10 9:33
 */
public class TrajectoryConfigUtils {
    /**
     * 初始化工程的轨迹配置
     * <p>
     * 采样方式
     * 采样率
     * 轨迹颜色
     *
     * @param dbProject
     */
    public static void initDefault(DBProject dbProject) {
        //设置工程的轨迹配置
        if (TextUtils.isEmpty(dbProject.getTrajectoryConfig())) {
            int defaultType = TrajectoryConfig.TYPE_BY_TIME;
            int defaultColor = TrajectoryConfig.DEFAULT_COLOR;
            int defaultLineWidth = TrajectoryConfig.DEFAULT_LINE_WIDTH;
            updateConfig(dbProject, new TrajectoryConfig(defaultLineWidth, defaultType, 1, "秒", defaultColor));
        }
    }

    /**
     * 修改轨迹配置
     *
     * @param dbProject        工程对象
     * @param trajectoryConfig 轨迹配置
     */
    public static void updateConfig(DBProject dbProject, TrajectoryConfig trajectoryConfig) {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        //设置工程的轨迹配置
        dbProject.setTrajectoryConfig(JsonParser.toJson(trajectoryConfig));
        dbProjectDao.insertOrReplace(dbProject);
    }

    /**
     * 获取轨迹配置
     *
     * @param dbProject 工程对象
     */
    public static TrajectoryConfig getConfig(DBProject dbProject) {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        DBProject newer = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Id.eq(dbProject.getId())).unique();
        return JsonParser.fromJson(newer.getTrajectoryConfig(), TrajectoryConfig.class);
    }

}
