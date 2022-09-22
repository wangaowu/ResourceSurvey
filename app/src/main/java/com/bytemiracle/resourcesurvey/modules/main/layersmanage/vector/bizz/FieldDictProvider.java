package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz;

import android.content.Context;
import android.util.ArrayMap;

import com.bytemiracle.resourcesurvey.cache_greendao.DBFieldDictDao;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.FieldDict;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.osmdroid.customImpl.geopackage.load.LoadGeopackageImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/11 13:44
 */
public class FieldDictProvider {
    private static final String TAG = "FieldDictProvider";

    /**
     * 获取字典配置
     *
     * @param context
     * @param projectId
     * @return
     */
    public static Map<String, List<DBFieldDict>> getFieldDict(Context context, Long projectId) {
        String gpkgPath = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
        Map<String, List<String>> map = new LoadGeopackageImpl(context, new File(gpkgPath)).syncGetFieldNames();

        Map<String, List<DBFieldDict>> results = new ArrayMap<>();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String overlayName = entry.getKey();
            List<String> fieldNames = entry.getValue();
            ArrayList<DBFieldDict> dbFieldDicts = new ArrayList<>();
            for (String fieldName : fieldNames) {
                dbFieldDicts.add(getFieldDict(projectId, overlayName, fieldName));
            }
            results.put(overlayName, dbFieldDicts);
        }
        return results;
    }

    private static DBFieldDict getFieldDict(long projectId, String layerName, String fieldName) {
        DBFieldDictDao dbFieldDictDao = GreenDaoManager.getInstance().getDaoSession().getDBFieldDictDao();
        QueryBuilder<DBFieldDict> queryBuilder = dbFieldDictDao.queryBuilder();
        WhereCondition eq1 = DBFieldDictDao.Properties.ProjectId.eq(projectId);
        WhereCondition eq2 = DBFieldDictDao.Properties.LayerName.eq(layerName);
        WhereCondition eq3 = DBFieldDictDao.Properties.FieldName.eq(fieldName);
        WhereCondition queryCondition = queryBuilder.and(eq1, eq2, eq3);

        DBFieldDict dictDTO = queryBuilder.where(queryCondition).unique();
        if (dictDTO == null) {
            dictDTO = new DBFieldDict();
            dictDTO.setProjectId(projectId);
            dictDTO.setFieldName(fieldName);
            dictDTO.setLayerName(layerName);
        }
        dictDTO.setDictValues(FieldDict.convertPool2List(dictDTO.getFieldValuePool()));
        return dictDTO;
    }

    /**
     * 配置字典
     *
     * @param fieldDict
     */
    public static boolean insertOrReplaceFieldDict(DBFieldDict fieldDict) {
        DBFieldDictDao dbFieldDictDao = GreenDaoManager.getInstance().getDaoSession().getDBFieldDictDao();
        return dbFieldDictDao.insertOrReplace(fieldDict) > 0;
    }


    public static DBFieldDict getFieldDict(String name, List<DBFieldDict> dbFieldDicts) {
        for (DBFieldDict dbFieldDict : dbFieldDicts) {
            if (dbFieldDict.getFieldName().equalsIgnoreCase(name)) {
                return dbFieldDict;
            }
        }
        return null;
    }

    public static String getNameWithCheckType(String name, int checkType) {
        return name + getCheckCnTypeExtraStr(checkType);
    }

    public static String getTypeWithCheckType(String name, String cnType, List<DBFieldDict> dbFieldDicts) {
        for (DBFieldDict dbFieldDict : dbFieldDicts) {
            if (dbFieldDict.getFieldName().equalsIgnoreCase(name)) {
                return cnType + getCheckCnTypeExtraStr(dbFieldDict.getCheckType());
            }
        }
        return cnType;
    }

    public static String getCheckCnTypeExtraStr(int checkType) {
        switch (checkType) {
            case DBFieldDict.TYPE_SINGLE_CHECK:
                return "[单选]";
            case DBFieldDict.TYPE_MULTI_CHECK:
                return "[多选]";
            default:
                return "";
        }
    }
}
