package org.jts.utils

import android.graphics.Rect
import org.jts.converter.JTSGeometryConverter
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.overlay.utils.MapConstant
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon

/**
 * 类功能：图形计算工具类
 *
 * @author gwwang
 * @date 2022/3/4 13:59
 */
class JTSUtils {

    companion object {

        const val TAG = "GeometryCalc"

        /**
         * 判断自相交
         * @param geoPoints 图形1的点集合
         * @return 合并之后的结果
         */
        open fun isSelfIntersection(geoPoints: List<GeoPoint>): Boolean {
            val converter = JTSGeometryConverter.instance()
            val jtsPolygon1 = converter.directFromOsmGeoPoints(geoPoints)
            return !jtsPolygon1.isSimple
        }

        /**
         * 合并图形
         * @param geoPoints1 图形1的点集合
         * @param geoPoints2 图形2的点集合
         * @return 合并之后的结果
         */
        open fun unionGeometries(
            geoPoints1: List<GeoPoint>,
            geoPoints2: List<GeoPoint>
        ): List<GeoPoint> {
            val converter = JTSGeometryConverter.instance()
            val jtsPolygon1 = converter.directFromOsmGeoPoints(geoPoints1)
            val jtsPolygon2 = converter.directFromOsmGeoPoints(geoPoints2)
            val jtsUnionGeometry = jtsPolygon1.union(jtsPolygon2)
            return converter.parseJtsGeometry(jtsUnionGeometry)
        }

        /**
         * 是否有交集
         * @param geoPoints1 图形1的点集合
         * @param geoPoints2 图形2的点集合
         * @return 是否含有交集
         */
        open fun hasIntersection(geoPoints1: List<GeoPoint>, geoPoints2: List<GeoPoint>): Boolean {
            val converter = JTSGeometryConverter.instance()
            val polygon1 = Polygon()
            polygon1.points = geoPoints1
            val jtsPolygon1 = converter.fromOsmPolygon(polygon1)
            val polygon2 = Polygon()
            polygon2.points = geoPoints2
            val jtsPolygon2 = converter.fromOsmPolygon(polygon2)
            return jtsPolygon1.intersects(jtsPolygon2)
        }

        /**
         * 是否点击到了Marker上
         * @param tapPoint 点击时的经纬度坐标
         * @param markerWidth marker的大小
         * @param onWhatOverlay 响应在哪个package图层上
         * @param mapView mapView对象
         */
        open fun isTapOnMarker(
            tapPoint: GeoPoint,
            markerWidth: Int,
            onWhatOverlay: PackageOverlay,
            mapView: MapView
        ): Boolean {
            val tapXYOnScreen = mapView.projection.toPixels(tapPoint, null)
            val dots = onWhatOverlay.items.filterIsInstance<Marker>().filter { it.isDisplayed }
            if (dots.isNotEmpty()) {
                dots.forEach {
                    val screenXY = mapView.projection.toPixels(it.position, null)
                    val l = screenXY.x - markerWidth / 2
                    val t = screenXY.y - markerWidth / 2
                    val r = screenXY.x + markerWidth / 2
                    val b = screenXY.y + markerWidth / 2

                    val dotBufferBounds = Rect(l, t, r, b)
                    dotBufferBounds.inset(
                        -MapConstant.INVOKE_CLICK_TOLERANCE,
                        -MapConstant.INVOKE_CLICK_TOLERANCE
                    )
                    if (dotBufferBounds.contains(tapXYOnScreen.x, tapXYOnScreen.y)) {
                        return true
                    }
                }
            }
            return false
        }


        /**
         * 判断点是否在线段上
         */
        open fun isLinestringPoint(geoPoint: GeoPoint, geoPoints: List<GeoPoint>): Boolean {
            if (geoPoints.size < 3) {
                return false
            }
            val polygon = Polygon()
            polygon.points = geoPoints
            val converter = JTSGeometryConverter.instance()
            val jtsLinestring = converter.fromOsmGeoPoints(geoPoints)
            val jtsCoordinate = converter.fromOsmGeoPoint(geoPoint)
            val intersection = jtsLinestring.intersection(jtsCoordinate)
            return if (intersection == null) {
                false
            } else {
                !intersection.isEmpty
            }
        }


        /**
         * 判断点在不在面上
         */
        open fun isInnerPoint(geoPoint: GeoPoint, geoPoints: List<GeoPoint>): Boolean {
            if (geoPoints.size < 3) {
                return false
            }
            val polygon = Polygon()
            polygon.points = geoPoints
            val converter = JTSGeometryConverter.instance()
            val jtsPolygon = converter.fromOsmPolygon(polygon)
            val jtsCoordinate = converter.fromOsmGeoPoint(geoPoint)
            return jtsPolygon.contains(jtsCoordinate)
        }

        /**
         * 获取最近线条的索引位置
         */
        open fun getSuitableIndexBySimpleGeometry(
            ringLinePoints: List<GeoPoint>,
            point0: GeoPoint
        ): Int {
            var shouldInsertIndex = ringLinePoints.lastIndex
            val trySimpleGeometries = buildSimpleGeometry(ringLinePoints, point0)
            if (trySimpleGeometries.isNotEmpty()) {
                val verticalFootAtNotStretch = findVerticalFootAtNotStretch(trySimpleGeometries)
                shouldInsertIndex = if (verticalFootAtNotStretch.isNotEmpty()) {
                    //取所有合适情况中，垂直距离最短的情况
                    verticalFootAtNotStretch.sortBy { it.projectDistance }
                    verticalFootAtNotStretch[0].insertIndex
                } else {
                    trySimpleGeometries[0].insertIndex
                }
            }
            return shouldInsertIndex
        }

        /**
         *查找新增点，与相邻点之间线段的垂足，不在其延伸线上
         */
        private fun findVerticalFootAtNotStretch(
            simpleGeometries: MutableList<CalcCache>
        ): MutableList<CalcCache> {
            //备注Pair:<projectDistance,insertIndex>
            val verticalFootAtNotStretchResults =
                mutableListOf<CalcCache>()
            simpleGeometries.forEach {
                val allCoordinates = it.jtsPolygon.exteriorRing.coordinates
                val insertedCoordinate = allCoordinates[it.insertIndex]
                val preCoordinate =
                    if (it.insertIndex == 0) allCoordinates.last() else allCoordinates[it.insertIndex - 1]
                val nextCoordinate =
                    if (it.insertIndex == allCoordinates.lastIndex) allCoordinates.first() else allCoordinates[it.insertIndex + 1]
                //构建线段，并计算垂足
                val lineSegment = JTSGeometryConverter.instance()
                    .toLineSegment(arrayOf(preCoordinate, nextCoordinate))
                val projectPoint = lineSegment.project(insertedCoordinate)
                //判断垂足在不在线段上(本质为切割线段)
                val splitFraction = lineSegment.segmentFraction(projectPoint)
                if (splitFraction != 1.0 && splitFraction != 0.0) {
                    //在线段上的话，计算垂直距离
                    it.projectDistance = projectPoint.distance(insertedCoordinate)
                    verticalFootAtNotStretchResults.add(it)
                }
            }
            return verticalFootAtNotStretchResults
        }

        /**
         * 构建一个SimpleGeometry
         */
        private fun buildSimpleGeometry(
            points: List<GeoPoint>,
            newPoint: GeoPoint
        ): MutableList<CalcCache> {
            val simpleGeometries = mutableListOf<CalcCache>()
            val pointCount = points.size
            for (tryInsertIndex in 0..pointCount) {
                //构建插入点、封闭点集合
                val copyPoints = points.map { GeoPoint(it.latitude, it.longitude) }.toMutableList()
                copyPoints.add(tryInsertIndex, newPoint)
                if (copyPoints.first() != copyPoints.last()) copyPoints.add(copyPoints.first())
                //将osmDroid的geometry转换为jts的geometry
                val osmPolygon = Polygon()
                osmPolygon.points = copyPoints
                val jtsPolygon = JTSGeometryConverter.instance().fromOsmPolygon(osmPolygon)
                //有关JTS::simple的定义，请阅读父类注释
                if (jtsPolygon.isSimple) {
                    simpleGeometries.add(CalcCache(jtsPolygon, tryInsertIndex, 0.0))
                }
            }
            return simpleGeometries
        }

        /**
         * 线分割geometry
         */
        fun divideGeometry(
            ringPts: List<GeoPoint>,
            holes: List<List<GeoPoint>>,
            divideLinePts: List<GeoPoint>
        ): List<org.locationtech.jts.geom.Polygon> {
            val converter = JTSGeometryConverter.instance()
            val jtsPolygon = converter.fromOsmGeoPoints(ringPts, holes)
            val jtsLineString = converter.fromOsmGeoPoints(divideLinePts)
            return GeometrySplit.splitPolygon(jtsPolygon, jtsLineString)
        }
    }

    //计算过程中的数据封装
    private data class CalcCache(
        //构建的jts图形
        val jtsPolygon: org.locationtech.jts.geom.Polygon,

        //被插入的索引位置
        val insertIndex: Int,

        //垂直距离
        var projectDistance: Double
    )
}