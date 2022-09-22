package org.osmdroid.create

import android.graphics.drawable.Drawable
import com.bytemiracle.base.framework.utils.XToastUtils
import mil.nga.sf.Geometry
import mil.nga.sf.Point
import mil.nga.sf.Polygon
import org.osmdroid.defaultImpl.R
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * 类功能：创建点图形
 *
 * @author gwwang
 * @date 2022/5/20 0020 13:46
 */
class CreatePointImpl(val mapView: MapView, overlayName: String) : ICreateGeometry {
    private val measureGraphicOverlay = PackageOverlay(overlayName)

    //点符号
    private var rectDotDrawable: Drawable? = null

    //图形上的点
    private var geoPoint: GeoPoint? = null

    //绘制时需要监听事件的图层
    private val drawPointEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let {
                geoPoint = p
                drawGraphic()
                return true
            }
            return false
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    init {
        rectDotDrawable = mapView.context.resources.getDrawable(R.drawable.rect_edit_point)
        if (!mapView.overlayManager.overlays().stream().anyMatch { it is MapEventsOverlay }) {
            mapView.overlayManager.add(drawPointEventsOverlay)
        }
        if (!mapView.overlayManager.overlays().stream().anyMatch { it === measureGraphicOverlay }) {
            mapView.overlayManager.add(measureGraphicOverlay)
        }
    }

    private fun drawGraphic() {
        geoPoint?.let {
            measureGraphicOverlay.items.clear()
            measureGraphicOverlay.add(buildDot(geoPoint!!))
        }
        mapView.invalidate()
    }

    private fun buildDot(p: GeoPoint): Marker {
        val dot = Marker(mapView)
        dot.position = p
        dot.icon = rectDotDrawable
        dot.setAnchor(.5f, .5f) //偏移的是BitmapBounds宽度的倍数
        dot.setInfoWindow(null)
        return dot
    }

    override fun getCreatedGeometry(): Geometry {
        geoPoint?.let {
            return Point(geoPoint!!.longitude, geoPoint!!.latitude)
        }
        XToastUtils.info("请点击绘制一个点!")
        return Polygon() //empty geometry
    }

    override fun cancelDrawEventListener() {
        mapView.overlayManager.remove(drawPointEventsOverlay)
    }

    override fun clearGraphicInfo() {
        geoPoint = null
        measureGraphicOverlay.items.clear()
        mapView.overlayManager.remove(measureGraphicOverlay)
        mapView.overlayManager.remove(drawPointEventsOverlay)
        mapView.invalidate()
    }

    override fun removeEndMeasureNode() {
        // do nothing on point mode
    }

    override fun redoCachedNodes() {
        // do nothing on point mode
    }
}