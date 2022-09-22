package org.osmdroid.overlay.converter

import android.graphics.Color
import mil.nga.sf.LineString
import mil.nga.sf.Point
import mil.nga.sf.Polygon
import org.osmdroid.overlay.render.IWMarker
import org.osmdroid.overlay.render.IWPolygon
import org.osmdroid.overlay.render.IWPolyline
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.util.*

/**
 * 类功能：使用基础的点进行图形转换
 *
 * @author gwwang
 * @date 2022/2/22 17:26
 */
open class GeometryConverter {

    protected fun toGeoPoint(point: Point): GeoPoint {
        return GeoPoint(point.y, point.x)
    }

    protected fun toPolygon(polygon: Polygon): IWPolygon {
        val newPolygon = IWPolygon()
        val pts: MutableList<GeoPoint> = ArrayList()
        val holes: MutableList<List<GeoPoint>> = ArrayList()
        val rings = polygon.rings
        if (rings.isNotEmpty()) {
            // Add the polygon points
            val polygonLineString = rings[0]
            for (point in polygonLineString.points) {
                pts.add(toGeoPoint(point))
            }
            // Add the holes
            for (i in 1 until rings.size) {
                val hole = rings[i]
                val holeLatLngs: MutableList<GeoPoint> = ArrayList()
                for (point in hole.points) {
                    holeLatLngs.add(toGeoPoint(point))
                }
                holes.add(holeLatLngs)
            }
        }
        newPolygon.points = pts
        newPolygon.holes = holes
        return newPolygon
    }

    protected fun toPolyline(lineString: LineString): IWPolyline {
        val line = IWPolyline()
        val pts: MutableList<GeoPoint> = mutableListOf()
        for (point in lineString.points) {
            pts.add(toGeoPoint(point))
        }
        line.setPoints(pts)
        return line
    }

    fun fromGpkgMark(mapView: MapView, position: Point, markerContent: String): IWMarker {
        val iwMarker = IWMarker(mapView)
        iwMarker.position = toGeoPoint(position)
        iwMarker.infoWindow = null
        iwMarker.setTextIcon(markerContent)
        return iwMarker
    }
}