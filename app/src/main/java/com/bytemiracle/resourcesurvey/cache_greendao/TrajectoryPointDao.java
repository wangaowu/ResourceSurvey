package com.bytemiracle.resourcesurvey.cache_greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryPoint;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TRAJECTORY_POINT".
*/
public class TrajectoryPointDao extends AbstractDao<TrajectoryPoint, Long> {

    public static final String TABLENAME = "TRAJECTORY_POINT";

    /**
     * Properties of entity TrajectoryPoint.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property BelongId = new Property(1, Long.class, "belongId", false, "BELONG_ID");
        public final static Property RecordTime = new Property(2, long.class, "recordTime", false, "RECORD_TIME");
        public final static Property Longitude = new Property(3, double.class, "longitude", false, "LONGITUDE");
        public final static Property Latitude = new Property(4, double.class, "latitude", false, "LATITUDE");
        public final static Property Altitude = new Property(5, double.class, "altitude", false, "ALTITUDE");
    }


    public TrajectoryPointDao(DaoConfig config) {
        super(config);
    }
    
    public TrajectoryPointDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TRAJECTORY_POINT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"BELONG_ID\" INTEGER," + // 1: belongId
                "\"RECORD_TIME\" INTEGER NOT NULL ," + // 2: recordTime
                "\"LONGITUDE\" REAL NOT NULL ," + // 3: longitude
                "\"LATITUDE\" REAL NOT NULL ," + // 4: latitude
                "\"ALTITUDE\" REAL NOT NULL );"); // 5: altitude
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TRAJECTORY_POINT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TrajectoryPoint entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long belongId = entity.getBelongId();
        if (belongId != null) {
            stmt.bindLong(2, belongId);
        }
        stmt.bindLong(3, entity.getRecordTime());
        stmt.bindDouble(4, entity.getLongitude());
        stmt.bindDouble(5, entity.getLatitude());
        stmt.bindDouble(6, entity.getAltitude());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TrajectoryPoint entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long belongId = entity.getBelongId();
        if (belongId != null) {
            stmt.bindLong(2, belongId);
        }
        stmt.bindLong(3, entity.getRecordTime());
        stmt.bindDouble(4, entity.getLongitude());
        stmt.bindDouble(5, entity.getLatitude());
        stmt.bindDouble(6, entity.getAltitude());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public TrajectoryPoint readEntity(Cursor cursor, int offset) {
        TrajectoryPoint entity = new TrajectoryPoint( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // belongId
            cursor.getLong(offset + 2), // recordTime
            cursor.getDouble(offset + 3), // longitude
            cursor.getDouble(offset + 4), // latitude
            cursor.getDouble(offset + 5) // altitude
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TrajectoryPoint entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setBelongId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setRecordTime(cursor.getLong(offset + 2));
        entity.setLongitude(cursor.getDouble(offset + 3));
        entity.setLatitude(cursor.getDouble(offset + 4));
        entity.setAltitude(cursor.getDouble(offset + 5));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(TrajectoryPoint entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(TrajectoryPoint entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TrajectoryPoint entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
