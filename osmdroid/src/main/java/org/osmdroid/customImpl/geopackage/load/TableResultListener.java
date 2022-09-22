package org.osmdroid.customImpl.geopackage.load;

import java.util.Map;

/**
 * 类功能：表的加载
 *
 * @author gwwang
 * @date 2022/2/23 16:30
 */
public abstract class TableResultListener {

    /**
     * @param tableResult < tableName, tableResult>
     */
    public abstract void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult);
}
