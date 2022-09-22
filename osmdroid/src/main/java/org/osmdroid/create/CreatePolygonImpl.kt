package org.osmdroid.create

import com.bytemiracle.base.framework.utils.XToastUtils
import com.bytemiracle.base.framework.utils.common.ListUtils
import mil.nga.sf.Geometry
import mil.nga.sf.LineString
import mil.nga.sf.Point
import mil.nga.sf.Polygon
import org.osmdroid.measure.OsmMeasurePresenter
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.stream.Collectors

/**
 * 创建polygon图形
 */
class CreatePolygonImpl(
    mapView: MapView,
    overlayName: String
) : OsmMeasurePresenter(mapView, overlayName) {

    override fun getCreatedGeometry(): Geometry {
        //1.自动完成闭合
        autoCloseMeasureEndNode()
        //2.回调有效图形
        val drawPoints: List<GeoPoint> = ArrayList<GeoPoint>(getDrawPoints())
        if (ListUtils.isEmpty(drawPoints) || drawPoints.size < 3) {
            XToastUtils.info("不能添加无效的多边形!")
        } else {
            val polygon = buildPolygon(drawPoints)
            return if (polygon.isSimple) polygon else {
                XToastUtils.info("多边形不允许自相交!")
                Polygon()
            }
        }
        return Polygon()
    }

    private fun buildPolygon(drawPoints: List<GeoPoint>): Polygon {
        val sfPoints = drawPoints.stream()
            .map { Point(it.longitude, it.latitude) }
            .collect(Collectors.toList())
        //新建时polygon只支持单面，没有hole,可以在该图形的编辑功能内添加hole
        return Polygon(LineString(sfPoints))
    }
}