package org.osmdroid.overlay.render

import android.text.TextUtils
import org.osmdroid.overlay.bean.options.MarkerOptions
import org.osmdroid.overlay.bean.options.OsmRenderOption
import org.osmdroid.overlay.bean.options.PolygonOptions
import org.osmdroid.overlay.bean.options.PolylineOptions
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * 类功能：风格设置类
 *
 * @author gwwang
 * @date 2022/2/22 17:27
 */
open class OsmdroidOverlayStyleUtils(
    private val renderOption: OsmRenderOption,
    val mapView: MapView
) {

    private val makerOptions: MarkerOptions = renderOption.markerOption
    private val polylineOptions: PolylineOptions = renderOption.polyLineOption
    private val polygonOptions: PolygonOptions = renderOption.polygonOption

    fun setMarkerStyle(geoPoint: GeoPoint): IWMarker {
        val newMarker = IWMarker(mapView)
        newMarker.position = geoPoint
        newMarker.icon = makerOptions.icon
        newMarker.title = makerOptions.title
        newMarker.alpha = makerOptions.alpha
        newMarker.infoWindow = null
        if (!TextUtils.isEmpty(makerOptions.text)) {
            newMarker.setTextIcon(makerOptions.text)
        }
        newMarker.setSelectOptions(makerOptions)
        return newMarker
    }

    fun setPolylineStyle(line: IWPolyline) {
        line.title = polylineOptions.title
        line.isGeodesic = polylineOptions.isGeodesic
        line.setSelectOptions(polylineOptions)
    }

    fun setPolygonStyle(polygon: IWPolygon) {
        polygon.setSelectOptions(polygonOptions)
    }
}