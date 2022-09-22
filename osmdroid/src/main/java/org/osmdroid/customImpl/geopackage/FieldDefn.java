package org.osmdroid.customImpl.geopackage;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.sf.GeometryType;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/25 9:59
 */
public class FieldDefn {
    public static Map<String, String> fieldMap = new LinkedHashMap<String, String>() {{
        put("字符型", "TEXT");
        put("浮点型", "DOUBLE");
        put("整数型", "INTEGER");
        put("布尔型", "BOOLEAN");
        put("日期型", "DATE");
        put("日期时间型", "DATETIME");
    }};
    public static Map<String, String> geomTypeMap = new LinkedHashMap<String, String>() {{
        put("面图层", "MULTIPOLYGON");
        put("线图层", "MULTILINESTRING");
        put("点图层", "MULTIPOINT");
    }};
    private static final String TAG = "FieldDefn";
    /**
     * 字段名
     */
    public String fieldName;
    /**
     * 字段类型
     */
    public GeoPackageDataType fieldType;
    /**
     * 默认值
     */
    public Object defaultValue;
    /**
     * 图层类型
     */
    public GeometryType geometryType;
    /**
     * 是否是图形
     */
    public boolean isGeometryCol;

    private FieldDefn(String fieldName, GeoPackageDataType fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.defaultValue = matchDefaultValue(fieldType);
        this.isGeometryCol = false;
    }

    private FieldDefn(String fieldName, GeometryType geometryType) {
        this.fieldName = fieldName;
        this.geometryType = geometryType;
        this.fieldType = GeoPackageDataType.BLOB;
        this.isGeometryCol = true;
    }

    private Object matchDefaultValue(GeoPackageDataType fieldType) {
//        switch (fieldType) {
//            case INT:
//            case FLOAT:
//            case DOUBLE:
//            case INTEGER:
//            case SMALLINT:
//            case TINYINT:
//                return 0;
//            case TEXT:
//                return null;
//            case BOOLEAN:
//                return false;
//            case DATE:
//                return new Date();
//        }
        return null;
    }

    /**
     * 快速构建字段
     *
     * @param fieldName    字段名
     * @param fieldType    字段类型
     * @param geometryType 图形类型
     * @return
     */
    public static FieldDefn create(String fieldName, GeoPackageDataType fieldType, GeometryType geometryType) {
        if (geometryType == null || fieldType != GeoPackageDataType.BLOB) {
            return new FieldDefn(fieldName, fieldType);
        }
        return new FieldDefn(fieldName, geometryType);
    }

    /**
     * 查找定义geometry的字段
     *
     * @param fieldDefnList
     * @return
     */
    public static FieldDefn findGeometryCol(List<FieldDefn> fieldDefnList) {
        return fieldDefnList.stream()
                .filter(fieldDefn -> fieldDefn.isGeometryCol)
                .findFirst().get();
    }

    /**
     * 匹配表内定义的类型
     *
     * @param fieldTypeString
     * @return
     */
    public static GeoPackageDataType matchGeoPackageDataType(String fieldTypeString) {
        Class<GeoPackageDataType> geoPackageDataTypeClass = GeoPackageDataType.class;
        try {
            Field field = geoPackageDataTypeClass.getField(fieldTypeString);
            return (GeoPackageDataType) field.get(geoPackageDataTypeClass);
        } catch (Exception e) {
            Log.e(TAG, "matchGeoPackageDataType: " + e.getMessage());
        }
        return GeoPackageDataType.TEXT;
    }

    /**
     * 获取表内定义的类型
     *
     * @return
     */
    public static String[] getGeoPackageDataDefineTypes() {
        Class<GeoPackageDataType> geoPackageDataTypeClass = GeoPackageDataType.class;
        Field[] fields = geoPackageDataTypeClass.getFields();
        List<String> geoPackageDataTypes = Arrays.stream(fields)
                .map(field -> field.getName())
                .filter(s -> !"BLOB".equals(s)) //不再显示BLOB类型（因默认添加）
                .collect(Collectors.toList());
        return geoPackageDataTypes.toArray(new String[geoPackageDataTypes.size()]);
    }

    public static String getFieldMapKey(String str) {
        for (Map.Entry<String, String> entry : FieldDefn.fieldMap.entrySet()) {
            if (str.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static String getGeomTypeMapKey(String str) {
        for (Map.Entry<String, String> entry : FieldDefn.geomTypeMap.entrySet()) {
            if (str.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 获取表内定义的图层类型
     *
     * @return
     */
    public static String[] getDefineGeometryTypes() {
        return new String[]{"MULTIPOLYGON", "MULTILINESTRING", "MULTIPOINT"};
    }
}
