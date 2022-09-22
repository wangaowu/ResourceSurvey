package com.bytemiracle.resourcesurvey.common.dbbean;

import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.cache_greendao.DBRasterLayerDao;
import com.bytemiracle.resourcesurvey.cache_greendao.DaoSession;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.io.Serializable;
import java.util.List;

/**
 * 类功能：工程管理
 *
 * @author gwwang
 * @date 2021/5/26 15:33
 */
@Entity
public class DBProject implements Serializable {

    private static final long serialVersionUID = 1564654616132L;

    @Id(autoincrement = true)
    private Long id;
    /**
     * 项目名称
     */
    private String name;
    /**
     * 项目创建人
     */
    private String createBy;

    /**
     * 项目坐标系
     */
    private String prjWKT;

    /**
     * 项目创建时间
     */
    private long createTimestamps;

    /**
     * 当前工程
     */
    private int isLatestProject;

    /**
     * 栅格底图路径
     */
    @ToMany(referencedJoinProperty = "projectId")
    private List<DBRasterLayer> dbRasterLayers;

    /**
     * project的水印配置
     */
    private String projectMediaWaterMark;

    /**
     * feature的水印配置
     */
    private String featureMediaWaterMark;

    /**
     * 轨迹的配置
     */
    private String trajectoryConfig;

    /**
     * 渲染的配置
     */
    private String renderStyleConfig;

    /**
     * 附加字段
     */
    private String extra;

    public String getSpatialReferenceSimpleName() {
        return new SpatialSystemProcessor(prjWKT).getDisplayName();
    }

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1541593711)
    private transient DBProjectDao myDao;

    @Generated(hash = 871722365)
    public DBProject(Long id, String name, String createBy, String prjWKT,
                     long createTimestamps, int isLatestProject,
                     String projectMediaWaterMark, String featureMediaWaterMark,
                     String trajectoryConfig, String renderStyleConfig, String extra) {
        this.id = id;
        this.name = name;
        this.createBy = createBy;
        this.prjWKT = prjWKT;
        this.createTimestamps = createTimestamps;
        this.isLatestProject = isLatestProject;
        this.projectMediaWaterMark = projectMediaWaterMark;
        this.featureMediaWaterMark = featureMediaWaterMark;
        this.trajectoryConfig = trajectoryConfig;
        this.renderStyleConfig = renderStyleConfig;
        this.extra = extra;
    }

    @Generated(hash = 295926326)
    public DBProject() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateBy() {
        return this.createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getPrjWKT() {
        return this.prjWKT;
    }

    public void setPrjWKT(String prjWKT) {
        this.prjWKT = prjWKT;
    }

    public long getCreateTimestamps() {
        return this.createTimestamps;
    }

    public void setCreateTimestamps(long createTimestamps) {
        this.createTimestamps = createTimestamps;
    }

    public int getIsLatestProject() {
        return this.isLatestProject;
    }

    public void setIsLatestProject(int isLatestProject) {
        this.isLatestProject = isLatestProject;
    }

    public String getProjectMediaWaterMark() {
        return this.projectMediaWaterMark;
    }

    public void setProjectMediaWaterMark(String projectMediaWaterMark) {
        this.projectMediaWaterMark = projectMediaWaterMark;
    }

    public String getFeatureMediaWaterMark() {
        return this.featureMediaWaterMark;
    }

    public void setFeatureMediaWaterMark(String featureMediaWaterMark) {
        this.featureMediaWaterMark = featureMediaWaterMark;
    }

    public String getTrajectoryConfig() {
        return this.trajectoryConfig;
    }

    public void setTrajectoryConfig(String trajectoryConfig) {
        this.trajectoryConfig = trajectoryConfig;
    }

    public String getRenderStyleConfig() {
        return this.renderStyleConfig;
    }

    public void setRenderStyleConfig(String renderStyleConfig) {
        this.renderStyleConfig = renderStyleConfig;
    }

    public String getExtra() {
        return this.extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 497708784)
    public List<DBRasterLayer> getDbRasterLayers() {
        if (dbRasterLayers == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DBRasterLayerDao targetDao = daoSession.getDBRasterLayerDao();
            List<DBRasterLayer> dbRasterLayersNew = targetDao
                    ._queryDBProject_DbRasterLayers(id);
            synchronized (this) {
                if (dbRasterLayers == null) {
                    dbRasterLayers = dbRasterLayersNew;
                }
            }
        }
        return dbRasterLayers;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 485439430)
    public synchronized void resetDbRasterLayers() {
        dbRasterLayers = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1890115352)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDBProjectDao() : null;
    }


}
