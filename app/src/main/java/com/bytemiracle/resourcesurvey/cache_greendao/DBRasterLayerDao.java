package com.bytemiracle.resourcesurvey.cache_greendao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.bytemiracle.resourcesurvey.common.dbbean.DBRasterLayer;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DBRASTER_LAYER".
*/
public class DBRasterLayerDao extends AbstractDao<DBRasterLayer, Long> {

    public static final String TABLENAME = "DBRASTER_LAYER";

    /**
     * Properties of entity DBRasterLayer.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property ProjectId = new Property(1, Long.class, "projectId", false, "PROJECT_ID");
        public final static Property FilePath = new Property(2, String.class, "filePath", false, "FILE_PATH");
    }

    private Query<DBRasterLayer> dBProject_DbRasterLayersQuery;

    public DBRasterLayerDao(DaoConfig config) {
        super(config);
    }
    
    public DBRasterLayerDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DBRASTER_LAYER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"PROJECT_ID\" INTEGER," + // 1: projectId
                "\"FILE_PATH\" TEXT);"); // 2: filePath
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DBRASTER_LAYER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DBRasterLayer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long projectId = entity.getProjectId();
        if (projectId != null) {
            stmt.bindLong(2, projectId);
        }
 
        String filePath = entity.getFilePath();
        if (filePath != null) {
            stmt.bindString(3, filePath);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DBRasterLayer entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long projectId = entity.getProjectId();
        if (projectId != null) {
            stmt.bindLong(2, projectId);
        }
 
        String filePath = entity.getFilePath();
        if (filePath != null) {
            stmt.bindString(3, filePath);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public DBRasterLayer readEntity(Cursor cursor, int offset) {
        DBRasterLayer entity = new DBRasterLayer( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // projectId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // filePath
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DBRasterLayer entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setProjectId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setFilePath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(DBRasterLayer entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(DBRasterLayer entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DBRasterLayer entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "dbRasterLayers" to-many relationship of DBProject. */
    public List<DBRasterLayer> _queryDBProject_DbRasterLayers(Long projectId) {
        synchronized (this) {
            if (dBProject_DbRasterLayersQuery == null) {
                QueryBuilder<DBRasterLayer> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.ProjectId.eq(null));
                dBProject_DbRasterLayersQuery = queryBuilder.build();
            }
        }
        Query<DBRasterLayer> query = dBProject_DbRasterLayersQuery.forCurrentThread();
        query.setParameter(0, projectId);
        return query.list();
    }

}
