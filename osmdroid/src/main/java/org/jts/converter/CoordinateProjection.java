package org.jts.converter;

import org.jts.CoordinateTransform;
import org.locationtech.jts.geom.Coordinate;
import org.osmdroid.util.GeoPoint;

/**
 * 类功能：坐标投影
 *
 * @author gwwang
 * @date 2022/2/9 14:19
 */
public class CoordinateProjection {

    public static final int SRS_3857 = 3857;

    public Coordinate projection(GeoPoint geoPoint) {
        GeoPoint geoPoint1 = CoordinateTransform.to3857(geoPoint);
        return new Coordinate(geoPoint1.getLongitude(), geoPoint1.getLatitude());
    }

    public GeoPoint parseProjection(Coordinate coordinate) {
        GeoPoint point3857 = new GeoPoint(coordinate.y, coordinate.x);
        return CoordinateTransform.to4326(point3857, "epsg:3857");
    }
}
