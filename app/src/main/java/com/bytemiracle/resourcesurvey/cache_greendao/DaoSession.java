package com.bytemiracle.resourcesurvey.cache_greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.dbbean.DBRasterLayer;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.PrjTrajectory;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryPoint;

import com.bytemiracle.resourcesurvey.cache_greendao.DBFieldDictDao;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.cache_greendao.DBRasterLayerDao;
import com.bytemiracle.resourcesurvey.cache_greendao.PrjTrajectoryDao;
import com.bytemiracle.resourcesurvey.cache_greendao.TrajectoryPointDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dBFieldDictDaoConfig;
    private final DaoConfig dBProjectDaoConfig;
    private final DaoConfig dBRasterLayerDaoConfig;
    private final DaoConfig prjTrajectoryDaoConfig;
    private final DaoConfig trajectoryPointDaoConfig;

    private final DBFieldDictDao dBFieldDictDao;
    private final DBProjectDao dBProjectDao;
    private final DBRasterLayerDao dBRasterLayerDao;
    private final PrjTrajectoryDao prjTrajectoryDao;
    private final TrajectoryPointDao trajectoryPointDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dBFieldDictDaoConfig = daoConfigMap.get(DBFieldDictDao.class).clone();
        dBFieldDictDaoConfig.initIdentityScope(type);

        dBProjectDaoConfig = daoConfigMap.get(DBProjectDao.class).clone();
        dBProjectDaoConfig.initIdentityScope(type);

        dBRasterLayerDaoConfig = daoConfigMap.get(DBRasterLayerDao.class).clone();
        dBRasterLayerDaoConfig.initIdentityScope(type);

        prjTrajectoryDaoConfig = daoConfigMap.get(PrjTrajectoryDao.class).clone();
        prjTrajectoryDaoConfig.initIdentityScope(type);

        trajectoryPointDaoConfig = daoConfigMap.get(TrajectoryPointDao.class).clone();
        trajectoryPointDaoConfig.initIdentityScope(type);

        dBFieldDictDao = new DBFieldDictDao(dBFieldDictDaoConfig, this);
        dBProjectDao = new DBProjectDao(dBProjectDaoConfig, this);
        dBRasterLayerDao = new DBRasterLayerDao(dBRasterLayerDaoConfig, this);
        prjTrajectoryDao = new PrjTrajectoryDao(prjTrajectoryDaoConfig, this);
        trajectoryPointDao = new TrajectoryPointDao(trajectoryPointDaoConfig, this);

        registerDao(DBFieldDict.class, dBFieldDictDao);
        registerDao(DBProject.class, dBProjectDao);
        registerDao(DBRasterLayer.class, dBRasterLayerDao);
        registerDao(PrjTrajectory.class, prjTrajectoryDao);
        registerDao(TrajectoryPoint.class, trajectoryPointDao);
    }
    
    public void clear() {
        dBFieldDictDaoConfig.clearIdentityScope();
        dBProjectDaoConfig.clearIdentityScope();
        dBRasterLayerDaoConfig.clearIdentityScope();
        prjTrajectoryDaoConfig.clearIdentityScope();
        trajectoryPointDaoConfig.clearIdentityScope();
    }

    public DBFieldDictDao getDBFieldDictDao() {
        return dBFieldDictDao;
    }

    public DBProjectDao getDBProjectDao() {
        return dBProjectDao;
    }

    public DBRasterLayerDao getDBRasterLayerDao() {
        return dBRasterLayerDao;
    }

    public PrjTrajectoryDao getPrjTrajectoryDao() {
        return prjTrajectoryDao;
    }

    public TrajectoryPointDao getTrajectoryPointDao() {
        return trajectoryPointDao;
    }

}
