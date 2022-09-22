package org.osmdroid.measure

import android.graphics.drawable.Drawable
import org.jts.converter.JTSGeometryConverter
import org.jts.utils.MCollections
import org.jts.utils.PrivateNumberUtils
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.bean.MeasureMode
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.bean.OsmRenderStyle
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * 类功能：有关地图测量的实现
 *
 * @author gwwang
 * @date 2022/2/8 14:06
 */
class OsmMeasurePresenter2(val mapView: MapView, val overlayName: String) {

    private var measureMode: MeasureMode = MeasureMode.NONE
    private val measureGraphicOverlay = PackageOverlay(overlayName)

    //点符号
    private var rectDotDrawable: Drawable? = null

    //图形上的点
    private val measureDotList = arrayListOf<GeoPoint>()

    //绘制时需要监听事件的图层
    private val drawPointEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let {
                if (measureMode != MeasureMode.NONE) {
                    addNewMeasureNode(p)
                    return true
                }
            }
            return false
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    init {
        rectDotDrawable = mapView.context.resources.getDrawable(R.drawable.rect_edit_point)
    }

    private fun assertOnlyMapMeasureOverlay() {
        //事件图层
        if (!mapView.overlayManager.overlays().stream()
                .anyMatch { it === drawPointEventsOverlay }
        ) {
            mapView.overlayManager.add(drawPointEventsOverlay)
        }
        //绘制图层
        if (!mapView.overlayManager.overlays().stream()
                .anyMatch { it === measureGraphicOverlay }
        ) {
            mapView.overlayManager.add(measureGraphicOverlay)
        }
    }

    /**
     * 设置测量方式
     * @param measureMode 测量方式
     */
    open fun changeMeasureMode(measureMode: MeasureMode) {
        this.measureMode = measureMode

        clearGraphicInfo()
        assertOnlyMapMeasureOverlay()

        mapView.invalidate()
    }

    /**
     * 清除已有的测量信息
     */
    open fun clearGraphicInfo() {
        measureDotList.clear()
        measureGraphicOverlay.items.clear()
        mapView.overlayManager.remove(measureGraphicOverlay)
        mapView.overlayManager.remove(drawPointEventsOverlay)
        mapView.invalidate()
    }

    private fun addNewMeasureNode(p: GeoPoint) {
        measureDotList.add(p)
        drawGraphic()
    }

    private fun drawGraphic() {
        val outLine = drawBaseGraphic()
        when (measureMode) {
            MeasureMode.AREA -> {
                drawCenterAreaText(outLine)
            }
            MeasureMode.DISTANCE -> {
                drawCenterLengthText(outLine)
            }
            MeasureMode.CREATE -> {
                //do nothing
            }
            else -> {
                //do nothing
            }
        }
    }

    private fun drawBaseGraphic(): Polyline {
        if (measureMode == MeasureMode.AREA) {
            val outLine =
                drawDotPolygon(MCollections.ensureClosedPoints(measureDotList.toMutableList()))
            drawLineLengthText(MCollections.ensureClosedPoints(measureDotList.toMutableList()))
            mapView.invalidate()
            return outLine
        } else if (measureMode == MeasureMode.DISTANCE) {
            val outLine = drawDotPolygon(measureDotList)
            drawLineLengthText(measureDotList)
            mapView.invalidate()
            return outLine
        }
        return Polyline()
    }

    private fun drawDotPolygon(measureDotList: List<GeoPoint>): Polyline {
        measureGraphicOverlay.items.clear()

        var outLine = Polyline()

        if (measureDotList.size > 1) {
            outLine = buildPolyLine(measureDotList)
            measureGraphicOverlay.add(outLine)
        }

        measureDotList.forEach {
            measureGraphicOverlay.add(buildDot(it))
        }
        return outLine
    }

    private fun drawLineLengthText(dotList: List<GeoPoint>) {
        dotList.forEachIndexed { index, curPoint ->
            if (index > 0) {
                val prePoint = dotList[index - 1]
                drawEachLineLengthText(prePoint, curPoint)
            }
        }
    }

    private fun drawEachLineLengthText(prePoint: GeoPoint, curPoint: GeoPoint) {
        val distance = prePoint.distanceToAsDouble(curPoint)
        val distanceText = "${PrivateNumberUtils.getPlainDouble(distance, 2)}m"

        val centerPoint = GeoPoint.fromCenterBetween(prePoint, curPoint)
        measureGraphicOverlay.add(buildText(distanceText, centerPoint))
    }

    private fun drawCenterAreaText(outLine: Polyline) {
        val boundsCenter = outLine.bounds.centerWithDateLine
        val jtsPolygon = JTSGeometryConverter.instance().fromOsmClosedPolyline(outLine)
        val areaText = "面积:${PrivateNumberUtils.getPlainDouble(jtsPolygon.area, 2)}㎡"
        measureGraphicOverlay.add(buildText(areaText, boundsCenter))
    }

    private fun drawCenterLengthText(outLine: Polyline) {
        val distanceText = "总长:${PrivateNumberUtils.getPlainDouble(outLine.distance, 2)}m"
        val boundsCenter = outLine.bounds.centerWithDateLine
        measureGraphicOverlay.add(buildText(distanceText, boundsCenter))
    }

    private fun buildText(text: String, centerPoint: GeoPoint): Marker {
        val textMarker = Marker(mapView)
        textMarker.position = centerPoint
        textMarker.setTextIcon(text)
        textMarker.setInfoWindow(null)
        return textMarker
    }

    private fun buildDot(p: GeoPoint): Marker {
        val dot = Marker(mapView)
        dot.position = p
        dot.icon = rectDotDrawable
        dot.setAnchor(.5f, .5f) //偏移的是BitmapBounds宽度的倍数
        dot.setInfoWindow(null)
        return dot
    }

    private fun buildPolyLine(measureDotList: List<GeoPoint>): Polyline {
        val polyline = Polyline()
        polyline.setPoints(measureDotList)
        polyline.outlinePaint.strokeWidth =
            OsmRenderStyle.DEFAULT_STYLE.selectBoundLineWidth.toFloat()
        polyline.outlinePaint.color = OsmRenderStyle.DEFAULT_STYLE.selectBoundColor
        polyline.infoWindow = null
        return polyline
    }
}