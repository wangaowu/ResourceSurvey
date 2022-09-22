package org.osmdroid.customImpl.convert.transform;

import android.util.Log;

import org.jts.CoordinateTransform;
import org.osmdroid.customImpl.convert.reader.GeometryWktReader;
import org.osmdroid.customImpl.convert.reader.WktSplit;
import org.osmdroid.customImpl.convert.transform.inter.GeometryDataPolicy;
import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * 类功能：将shpGeometry的坐标数据转换为wgs84的坐标
 *
 * @author gwwang
 * @date 2022/3/25 16:35
 */
public class To84GeometryWktImpl implements GeometryDataPolicy {
    private static final String TAG = "To84GeometryWktImpl";

    /**
     * @param srcGeometryWkt              shp文件内geometry的wkt
     * @param srcGeometrySpatialReference shp文件内geometry的坐标系
     * @return wgs84坐标系下的geometry的wkb
     */
    @Override
    public String getTransformedGeometryWkt(String srcGeometryWkt, SpatialReferenceCompact srcGeometrySpatialReference) {
        StringBuilder newWktStringBuilder = new StringBuilder();
        List<WktSplit> wktSplits = new GeometryWktReader(srcGeometryWkt).getWktSplits();
        for (WktSplit wktSplit : wktSplits) {
            String content = wktSplit.getContent();
            if (wktSplit.getCase_() == WktSplit.Case.POINT) {
                double[] point = wktSplit.getPoint();
                GeoPoint srcGeoPoint = new GeoPoint(point[1], point[0], point[2]);
                GeoPoint geoPoint = CoordinateTransform.to4326(srcGeoPoint, srcGeometrySpatialReference.getSpatialReferenceProj4());
                content = geoPoint.getLongitude() + " " + geoPoint.getLatitude() + " " + geoPoint.getAltitude();
            }
            newWktStringBuilder.append(content);
        }
        return newWktStringBuilder.toString();
    }
}
