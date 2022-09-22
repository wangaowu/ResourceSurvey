package org.osmdroid.create

import android.graphics.drawable.Drawable
import com.bytemiracle.base.framework.utils.XToastUtils
import com.bytemiracle.base.framework.utils.common.ListUtils
import mil.nga.sf.Geometry
import mil.nga.sf.LineString
import mil.nga.sf.Point
import mil.nga.sf.Polygon
import org.osmdroid.defaultImpl.R
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.bean.OsmRenderStyle
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.stream.Collectors

/**
 * 类功能：创建线图形
 *
 * @author gwwang
 * @date 2022/5/20 0020 13:46
 */
class CreateLineImpl(val mapView: MapView, overlayName: String) : ICreateGeometry {

    private val measureGraphicOverlay = PackageOverlay(overlayName)

    //点符号
    private var rectDotDrawable: Drawable? = null

    //缓存点(恢复功能)
    private val cachedDotList = arrayListOf<GeoPoint>()

    //图形上的点
    private val measureDotList = arrayListOf<GeoPoint>()

    //绘制时需要监听事件的图层
    private val drawPointEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
            p?.let {
                cachedDotList.clear()
                addNewMeasureNode(p)
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

    /**
     * 获取绘制的点
     */
    fun getDrawPoints(): List<GeoPoint> {
        return measureDotList;
    }

    /**
     * 清除已有的测量信息
     */
    override fun clearGraphicInfo() {
        measureDotList.clear()
        measureGraphicOverlay.items.clear()
        mapView.overlayManager.remove(measureGraphicOverlay)
        mapView.overlayManager.remove(drawPointEventsOverlay)
        mapView.invalidate()
    }

    /**
     * 移除末尾节点
     */
    override fun removeEndMeasureNode() {
        if (measureDotList.isNotEmpty()) {
            val endNode = measureDotList[measureDotList.size - 1]
            cachedDotList.add(endNode)
            measureDotList.remove(endNode)
            drawGraphic()
        }
    }

    /**
     * 自动闭合节点
     */
    override fun redoCachedNodes() {
        if (!ListUtils.isEmpty(cachedDotList)) {
            val takeNode = cachedDotList.last()
            cachedDotList.remove(takeNode)
            addNewMeasureNode(takeNode)
        }
    }

    override fun getCreatedGeometry(): Geometry {
        return if (!ListUtils.isEmpty(measureDotList) && measureDotList.size >= 2) {
            val sfPoints = measureDotList.stream()
                .map { Point(it.longitude, it.latitude) }
                .collect(Collectors.toList())
            LineString(sfPoints)
        } else {
            XToastUtils.info("不能添加无效的线条!")
            Polygon() //empty geometry
        }
    }

    /**
     * 移除事件监听
     */
    override fun cancelDrawEventListener() {
        mapView.overlayManager.remove(drawPointEventsOverlay)
    }

    private fun addNewMeasureNode(p: GeoPoint) {
        measureDotList.add(p)
        drawGraphic()
    }

    private fun drawGraphic() {
        drawDotLinestring(measureDotList)
        mapView.invalidate()
    }

    private fun drawDotLinestring(measureDotList: List<GeoPoint>): Polyline {
        measureGraphicOverlay.items.clear()

        var outLine = Polyline()

        if (measureDotList.size >= 2) {
            outLine = buildPolyLine(measureDotList)
            measureGraphicOverlay.add(outLine)
        }

        measureDotList.forEach {
            measureGraphicOverlay.add(buildDot(it))
        }
        return outLine
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