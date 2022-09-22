package org.jts.utils

import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.geom.util.LineStringExtracter
import org.locationtech.jts.operation.polygonize.Polygonizer

/**
 * 类功能：图形拆分
 * （和cut不同，cut是rectangle与图形的裁剪）
 *
 * @author gwwang
 * @date 2022/6/10 14:12
 */
class GeometrySplit {
    companion object {
        const val TAG = "GeometrySplit"

        /**
         * 将线条们识别出polygons
         * @param boundaryLines 混乱线条
         * @param extractOnlyPolygon 是否只要polygon
         */
        private fun recognizePolygons(
            boundaryLines: Geometry,
            extractOnlyPolygon: Boolean
        ): List<Polygon> {
            //将非意义的geometry（线条的堆放）拆分出lines
            val lines: List<*> = LineStringExtracter.getLines(boundaryLines)
            val polygonizer = Polygonizer(extractOnlyPolygon)
            //识别lines
            polygonizer.add(lines)
            //获取识别lines之后的polygons
            return polygonizer.polygons as List<Polygon>
            //不要问问就是不知道
            //val polyArray: Array<Polygon> = GeometryFactory.toPolygonArray(polygonizer.polygons)
            //return boundaryLines.factory.createGeometryCollection(polyArray)
        }

        /**
         * 使用线条切分polygon
         * @param polygon 待拆分的图形
         * @param line 拆分的工具线条
         */
        open fun splitPolygon(polygon: Polygon, line: Geometry): List<Polygon> {
            //该操作是将polygon所有的边界线（含hole边界线）和拆分线组合
            //虽然这个组合是一个非意义的geometry，可理解为线条的堆放
            val allBoundaryWithLine: Geometry = polygon.boundary.union(line)
            val polygons = recognizePolygons(allBoundaryWithLine, extractOnlyPolygon = false)

            //过滤条件为： 仅返回待拆分图形内部的图形(忽略延长线所相交的图形)
            val innerPolygons = polygons
                .filter { polygon.contains(it.interiorPoint) }
                .toList()
            return if (innerPolygons.isNotEmpty()) innerPolygons else listOf(polygon)
        }
    }
}