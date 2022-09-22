package org.jts;

import android.util.Log;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.geom.util.GeometryTransformer;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.CoordinateTransformFactory;
import org.locationtech.proj4j.ProjCoordinate;
import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mil.nga.sf.wkt.GeometryReader;

/**
 * 类功能：坐标转换
 * 演变过程:
 * 1 初始准备使用geotools进行坐标转换，但发现内部强依赖java awt等非android集成的类，迁移改动量几乎不可能完成
 * 2 切换使用gdal进行坐标转换(实质使用proj4的c++代码)，但发现2.3.2,3.4.2的dll版本均ok，但so版本不行，推测gdal.so中并没有集成proj4的c++代码，重新编译除非在完全不能实现的情况下进行
 * 3 使用proj4的java版本，已经ok
 *
 * @author gwwang
 * @date 2022/2/15 11:34
 */
public class CoordinateTransform {
    private static final String TAG = "CoordinateTransform";

    //存储已经解析到的坐标算法(存储是为了避免再次解析csv文件)
    private static final Map<String, CoordinateReferenceSystem> referenceSystemDict = new HashMap<>();

    private static CoordinateReferenceSystem getCoordinateReferenceSystem(String referenceSystemString) {
        boolean isShort = referenceSystemString.contains(":");
        String key = isShort ? referenceSystemString.toUpperCase() : referenceSystemString;
        CoordinateReferenceSystem coordinateReferenceSystem = referenceSystemDict.get(key);
        if (coordinateReferenceSystem == null) {
            CRSFactory crsFactory = new CRSFactory();
            coordinateReferenceSystem = isShort ? crsFactory.createFromName(key) : crsFactory.createFromParameters(key, referenceSystemString);
            referenceSystemDict.put(key, coordinateReferenceSystem);
        }
        return coordinateReferenceSystem;
    }

    /**
     * 转换Coordinate
     *
     * @param srcCoordinate             原始坐标
     * @param srcReferenceSystemString  原始坐标系
     * @param destReferenceSystemString 目标坐标系
     * @return
     */
    public static ProjCoordinate transformCoordinate(ProjCoordinate srcCoordinate, String srcReferenceSystemString, String destReferenceSystemString) {
        CoordinateReferenceSystem srcCoordinateReferenceSystem = getCoordinateReferenceSystem(srcReferenceSystemString);
        CoordinateReferenceSystem destCoordinateReferenceSystem = getCoordinateReferenceSystem(destReferenceSystemString);
        //转换
        ProjCoordinate result = new ProjCoordinate();
        org.locationtech.proj4j.CoordinateTransform transform = new CoordinateTransformFactory().createTransform(srcCoordinateReferenceSystem, destCoordinateReferenceSystem);
        transform.transform(srcCoordinate, result);
        return result;
    }

    /**
     * 转换geoPoint
     *
     * @param srcGeoPoint               原始坐标
     * @param srcReferenceSystemString  原始坐标系
     * @param destReferenceSystemString 目标坐标系
     * @return
     */
    public static GeoPoint transformGeoPoint(GeoPoint srcGeoPoint, String srcReferenceSystemString, String destReferenceSystemString) {
        ProjCoordinate srcCoordinate = new ProjCoordinate(srcGeoPoint.getLongitude(), srcGeoPoint.getLatitude(), srcGeoPoint.getAltitude());
        ProjCoordinate result = transformCoordinate(srcCoordinate, srcReferenceSystemString, destReferenceSystemString);
        return new GeoPoint(result.y, result.x);
    }

    /**
     * 转换到4326坐标
     *
     * @param geoPoint                 点坐标
     * @param srcReferenceSystemString 原始坐标系
     */
    public static GeoPoint to4326(GeoPoint geoPoint, String srcReferenceSystemString) {
        return transformGeoPoint(geoPoint, srcReferenceSystemString, "epsg:4326");
    }

    /**
     * 转换到目标坐标
     *
     * @param geoPoint                    点坐标
     * @param targetReferenceSystemString 原始坐标系
     */
    public static GeoPoint from4326(GeoPoint geoPoint, String targetReferenceSystemString) {
        return transformGeoPoint(geoPoint, "epsg:4326", targetReferenceSystemString);
    }

    /**
     * 转换到3857坐标
     *
     * @param geoPoint 点坐标
     */
    public static GeoPoint to3857(GeoPoint geoPoint) {
        return to3857(geoPoint, "epsg:4326");
    }

    /**
     * 转换到3857坐标
     *
     * @param geoPoint                 点坐标
     * @param srcReferenceSystemString 原始坐标系
     */
    public static GeoPoint to3857(GeoPoint geoPoint, String srcReferenceSystemString) {
        return transformGeoPoint(geoPoint, srcReferenceSystemString, "epsg:3857");
    }

    /**
     * 将3857的坐标图形转换为4326
     *
     * @param geometry3857
     * @return
     */
    public static Geometry to4326(Geometry geometry3857) {
        GeometryTransformer transformer = new GeometryTransformer() {
            @Override
            protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
                Coordinate[] results = new Coordinate[coords.size()];
                for (int i = 0; i < coords.size(); i++) {
                    Coordinate coord = coords.getCoordinate(i);
                    GeoPoint P4326 = to4326(new GeoPoint(coord.y, coord.x), "EPSG:3857");
                    results[i] = new Coordinate(P4326.getLongitude(), P4326.getLatitude());
                }
                return new CoordinateArraySequence(results);
            }
        };
        return transformer.transform(geometry3857);
    }

    /**
     * 将3857的坐标图形转换为4326
     *
     * @param jtsPolygons
     * @return
     */
    public static List<mil.nga.sf.Polygon> toSfPolygons(List<Polygon> jtsPolygons) {
        return jtsPolygons.stream()
                .map(geometry -> {
                    try {
                        Geometry jts4326 = to4326(geometry);
                        return (mil.nga.sf.Polygon) GeometryReader.readGeometry(jts4326.toText());
                    } catch (Exception e) {
                        Log.e(TAG, "JTS toSfPolygons, ERROR " + e.toString());
                        return new mil.nga.sf.Polygon();
                    }
                })
                .collect(Collectors.toList());
    }


    //------------------------------------☟-☟-☟-☟-☟-☟-☟-☟-通用的算法☟-☟-☟-☟-☟-☟-☟-☟-☟-☟-☟---------------------------------

    /**
     * 经纬度转平面坐标
     *
     * @param geoPoint
     * @return
     */
    public static GeoPoint to3857_(GeoPoint geoPoint) {
        double lat = geoPoint.getLatitude();
        double lon = geoPoint.getLongitude();
        double L = 6381372 * Math.PI * 2;//地球周长
        double W = L;// 平面展开后，x轴等于周长
        double H = L / 2;// y轴约等于周长一半
        double mill = 2.3;// 米勒投影中的一个常数，范围大约在正负2.3之间
        double x = lon * Math.PI / 180;// 将经度从度数转换为弧度
        double y = lat * Math.PI / 180;// 将纬度从度数转换为弧度
        y = 1.25 * Math.log(Math.tan(0.25 * Math.PI + 0.4 * y));// 米勒投影的转换
        // 弧度转为实际距离
        x = (W / 2) + (W / (2 * Math.PI)) * x;
        y = (H / 2) - (H / (2 * mill)) * y;
        return new GeoPoint(y, x);
    }
}
