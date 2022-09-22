package org.osmdroid.customImpl.convert.transform;

import org.jts.CoordinateTransform;
import org.osmdroid.customImpl.convert.reader.GeometryWktReader;
import org.osmdroid.customImpl.convert.reader.WktSplit;
import org.osmdroid.customImpl.convert.transform.inter.GeometryDataPolicy;
import org.osmdroid.util.GeoPoint;

import java.util.List;

/**
 * 类功能：从 WGS84 坐标到目标坐标系
 *
 * @author gwwang
 * @date 2022/3/25 16:38
 */
public class From84GeometryWktImpl implements GeometryDataPolicy {
    private static final String TAG = "From84GeometryWktImpl";

    /**
     * 从84坐标系转换为目标坐标系
     *
     * @param srcGeometryWkt                 待转换geometry的wkt
     * @param targetGeometrySpatialReference esri目标坐标系
     * @return
     */
    @Override
    public String getTransformedGeometryWkt(String srcGeometryWkt, SpatialReferenceCompact targetGeometrySpatialReference) {
        StringBuilder newWktStringBuilder = new StringBuilder();
        List<WktSplit> wktSplits = new GeometryWktReader(srcGeometryWkt).getWktSplits();
        for (WktSplit wktSplit : wktSplits) {
            String content = wktSplit.getContent();
            if (wktSplit.getCase_() == WktSplit.Case.POINT) {
                double[] point = wktSplit.getPoint();
                GeoPoint srcGeoPoint = new GeoPoint(point[1], point[0], point[2]);
                GeoPoint geoPoint = CoordinateTransform.from4326(srcGeoPoint, targetGeometrySpatialReference.getSpatialReferenceProj4());
                content = geoPoint.getLongitude() + " " + geoPoint.getLatitude() + " " + geoPoint.getAltitude();
            }
            newWktStringBuilder.append(content);
        }
        return newWktStringBuilder.toString();
    }
}
