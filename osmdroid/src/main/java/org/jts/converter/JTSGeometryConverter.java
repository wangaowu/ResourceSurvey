package org.jts.converter;

import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequenceFactory;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能：jts和openlayer的图形转化
 * 因参与运算，注意:
 * 该类转换出去的坐标系，全部都是espg:3857的平面投影坐标系
 *
 * @author gwwang
 * @date 2022/2/9 13:51
 */
public class JTSGeometryConverter extends CoordinateProjection {
    private static final String TAG = "JTSGeometryConverter";
    private static GeometryFactory GEOMETRY_FACTORY;

    static {
        //jts参与计算的图形，都是3857的坐标系统
        PrecisionModel precisionModel = new PrecisionModel();
        GEOMETRY_FACTORY = new GeometryFactory(precisionModel, SRS_3857);
    }

    private static JTSGeometryConverter instance = new JTSGeometryConverter();

    /**
     * 饿汉的单例
     *
     * @return
     */
    public static JTSGeometryConverter instance() {
        return instance;
    }

    /**
     * 将osm模块的点集合直接构建jts的图形
     *
     * @param geoPoints osm模块的点集合
     * @return
     */
    public Polygon directFromOsmGeoPoints(List<GeoPoint> geoPoints) {
        //将所有点转换为jts需要的类型
        LinearRing linearRing = toLinearRing(geoPoints);
        return new Polygon(linearRing, null, GEOMETRY_FACTORY);
    }

    /**
     * 获取jts图形的边界点(获取WGS:84坐标)
     *
     * @param jtsGeometry jts的图形
     * @return
     */
    public List<GeoPoint> parseJtsGeometry(Geometry jtsGeometry) {
        Coordinate[] coordinates = jtsGeometry.getCoordinates();
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (Coordinate coordinate : coordinates) {
            geoPoints.add(parseProjection(coordinate));
        }
        return geoPoints;
    }


    /**
     * 将osm模块的osmPolyline转换为jts模块的polygon
     *
     * @param osmPolyline osm模块的osmPolyline
     * @return
     */
    public Polygon fromOsmClosedPolyline(org.osmdroid.views.overlay.Polyline osmPolyline) {
        List<GeoPoint> outlinePoints = osmPolyline.getActualPoints();

        //将所有点转换为jts需要的类型
        LinearRing shell = toLinearRing(outlinePoints);
        return new Polygon(shell, null, GEOMETRY_FACTORY);
    }

    /**
     * 将osm模块的polygon转换为jts模块的polygon
     *
     * @param osmPolygon osm模块的polygon
     * @return
     */
    public Polygon fromOsmPolygon(org.osmdroid.views.overlay.Polygon osmPolygon) {
        List<GeoPoint> outlinePoints = osmPolygon.getActualPoints();
        List<List<GeoPoint>> holes = osmPolygon.getHoles();
        return fromOsmGeoPoints(outlinePoints, holes);
    }

    /**
     * 将osm模块的polygon转换为jts模块的polygon
     *
     * @param outlinePoints osm模块的外围点
     * @param holes         osm模块的孔点
     * @return
     */
    public Polygon fromOsmGeoPoints(List<GeoPoint> outlinePoints, List<List<GeoPoint>> holes) {
        //将所有点转换为jts需要的类型
        LinearRing shell = toLinearRing(outlinePoints);
        LinearRing[] jtsHoles = toLinearRings(holes);
        return new Polygon(shell, jtsHoles, GEOMETRY_FACTORY);
    }

    /**
     * 将osm模块的线段点转换为jts模块的linestring
     *
     * @param geoPoints
     * @return
     */
    public LineString fromOsmGeoPoints(List<GeoPoint> geoPoints) {
        Coordinate[] a = toCoordinateArray(geoPoints);
        CoordinateSequence coordinateSequence = CoordinateArraySequenceFactory.instance().create(a);
        return new LineString(coordinateSequence, GEOMETRY_FACTORY);
    }


    /**
     * 将osm模块的polygon转换为jts模块的Point
     *
     * @param geoPoint
     * @return
     */
    public Point fromOsmGeoPoint(GeoPoint geoPoint) {
        Coordinate coordinate = projection(geoPoint);
        CoordinateSequence coordinateSequence = CoordinateArraySequenceFactory.instance().create(new Coordinate[]{coordinate});
        return new Point(coordinateSequence, GEOMETRY_FACTORY);
    }

    /**
     * 将geoPoints转换为jts的LinearRing
     *
     * @param geoPoints
     * @return
     */
    public LinearRing toLinearRing(List<GeoPoint> geoPoints) {
        Coordinate[] a = toCoordinateArray(geoPoints);
        CoordinateSequence coordinateSequence = CoordinateArraySequenceFactory.instance().create(a);
        return new LinearRing(coordinateSequence, GEOMETRY_FACTORY);
    }

    /**
     * 将geoPoints转换为coordinate的数组
     *
     * @param geoPoints
     * @return
     */
    public Coordinate[] toCoordinateArray(List<GeoPoint> geoPoints) {
        List<Coordinate> coordinateList = toCoordinateList(geoPoints);
        Coordinate[] coordinateArray = new Coordinate[coordinateList.size()];
        coordinateArray = coordinateList.toArray(coordinateArray);
        return coordinateArray;
    }

    /**
     * 将geoPoints转换为coordinate的集合
     *
     * @param geoPoints
     * @return
     */
    public List<Coordinate> toCoordinateList(List<GeoPoint> geoPoints) {
        List<Coordinate> coordinates = new ArrayList<>();
        if (geoPoints != null) {
            for (GeoPoint geoPoint : geoPoints) {
                coordinates.add(projection(geoPoint));
            }
        }
        return coordinates;
    }

    private LinearRing[] toLinearRings(List<List<GeoPoint>> geoPointss) {
        if (geoPointss == null) {
            return new LinearRing[0];
        }
        LinearRing[] linearRings = new LinearRing[geoPointss.size()];
        for (int i = 0; i < linearRings.length; i++) {
            linearRings[i] = toLinearRing(geoPointss.get(i));
        }
        return linearRings;
    }

    /**
     * 转换到线段
     *
     * @param points
     * @return
     */
    public LineSegment toLineSegment(List<GeoPoint> points) {
        Coordinate[] coordinates = toCoordinateArray(points);
        return toLineSegment(coordinates);
    }

    /**
     * 构造线段
     *
     * @param coordinates
     * @return
     */
    public LineSegment toLineSegment(@NotNull Coordinate[] coordinates) {
        return new LineSegment(coordinates[0], coordinates[1]);
    }
}
