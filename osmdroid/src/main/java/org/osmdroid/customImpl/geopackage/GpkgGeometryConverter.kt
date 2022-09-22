package org.osmdroid.customImpl.geopackage

import mil.nga.geopackage.GeoPackageException
import mil.nga.sf.*
import org.osmdroid.overlay.bean.MultiOverlayWrapper
import org.osmdroid.overlay.bean.options.OsmRenderOption
import org.osmdroid.overlay.converter.GeometryConverter
import org.osmdroid.overlay.render.IWMarker
import org.osmdroid.overlay.render.IWPolygon
import org.osmdroid.overlay.render.IWPolyline
import org.osmdroid.overlay.render.OsmdroidOverlayStyleUtils
import org.osmdroid.views.MapView
import java.util.*
import java.util.stream.Collectors

/**
 * 类功能：使用wkt进行图形转换
 *
 * @author gwwang
 * @date 2022/2/22 15:02
 */
class GpkgGeometryConverter(renderOption: OsmRenderOption, private val mapView: MapView) :
    GeometryConverter() {

    private val styleUtils = OsmdroidOverlayStyleUtils(renderOption, mapView)

    /**
     * @param geometry
     * @return geometry的封装结构
     */
    fun fromGpkgGeometry(gpkgGeometry: Geometry): MultiOverlayWrapper {
        return matchShape(gpkgGeometry)
    }

    fun matchShape(geometry: Geometry): MultiOverlayWrapper {
        var overlayWrapper: MultiOverlayWrapper
        when (geometry.geometryType) {
            GeometryType.POINT -> {
                val geoPoint = toGeoPoint(geometry as Point)
                overlayWrapper =
                    MultiOverlayWrapper(
                        styleUtils.setMarkerStyle(geoPoint)
                    )
            }
            GeometryType.LINESTRING -> {
                val polyline = toPolyline(geometry as LineString)
                styleUtils.setPolylineStyle(polyline)
                overlayWrapper =
                    MultiOverlayWrapper(
                        polyline
                    )
            }
            GeometryType.POLYGON -> {
                val polygon = toPolygon(geometry as Polygon)
                styleUtils.setPolygonStyle(polygon)
                overlayWrapper =
                    MultiOverlayWrapper(
                        polygon
                    )
            }
            GeometryType.MULTIPOINT -> {
                val points = (geometry as MultiPoint).points.stream()
                    .map { styleUtils.setMarkerStyle(toGeoPoint(it)) }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        points as List<IWMarker>
                    )
            }
            GeometryType.MULTILINESTRING -> {
                val lineStrings = (geometry as MultiLineString).lineStrings.stream()
                    .map {
                        val lineString = toPolyline(it)
                        styleUtils.setPolylineStyle(lineString)
                        lineString
                    }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        lineStrings as List<IWPolyline>
                    )
            }
            GeometryType.MULTIPOLYGON -> {
                val polygons = (geometry as MultiPolygon).polygons.stream()
                    .map {
                        val polygon = toPolygon(it)
                        styleUtils.setPolygonStyle(polygon)
                        polygon
                    }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        polygons as List<IWPolygon>
                    )
            }
            GeometryType.CIRCULARSTRING -> {
                val polyline = toPolyline(geometry as LineString)
                styleUtils.setPolylineStyle(polyline)
                overlayWrapper =
                    MultiOverlayWrapper(
                        polyline
                    )
            }
            GeometryType.COMPOUNDCURVE -> {
                val polylineS = (geometry as CompoundCurve).lineStrings.stream()
                    .map {
                        val polyline = toPolyline(it)
                        styleUtils.setPolylineStyle(polyline)
                        polyline
                    }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        polylineS as List<IWPolyline>
                    )
            }
            GeometryType.POLYHEDRALSURFACE -> {
                val polygons = (geometry as PolyhedralSurface).polygons.stream()
                    .map {
                        val polygon = toPolygon(it)
                        styleUtils.setPolygonStyle(polygon)
                        polygon
                    }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        polygons as List<IWPolygon>
                    )
            }
            GeometryType.TIN -> {
                val polygons = (geometry as TIN).polygons.stream()
                    .map {
                        val polygon = toPolygon(it)
                        styleUtils.setPolygonStyle(polygon)
                        polygon
                    }
                    .collect(Collectors.toList())
                overlayWrapper =
                    MultiOverlayWrapper(
                        polygons as List<IWPolygon>
                    )
            }
            GeometryType.TRIANGLE -> {
                val polygon = toPolygon(geometry as Triangle)
                styleUtils.setPolygonStyle(polygon)
                overlayWrapper =
                    MultiOverlayWrapper(
                        polygon
                    )
            }
            //以下两种类型业务用不到，不被支持
            //GeometryType.GEOMETRYCOLLECTION
            //GeometryType.CURVEPOLYGON
            else -> throw GeoPackageException(
                "Unsupported Geometry Type: "
                        + geometry.geometryType.getName()
            )
        }
        return overlayWrapper
    }
}