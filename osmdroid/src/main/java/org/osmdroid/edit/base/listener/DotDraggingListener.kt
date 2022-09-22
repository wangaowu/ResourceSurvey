package org.osmdroid.edit.base.listener

import com.bytemiracle.base.framework.listener.CommonAsyncListener
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.overlay.utils.MapIWOverlayUtils
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

/**
 * 类功能：点的拖拽事件
 *
 * @author gwwang
 * @date 2022/5/5 16:36
 */
class DotDraggingListener(
    val dot: Marker,
    private val stepManager: EditStepManager<GeoPoint>,
    private val graphicOverlay: PackageOverlay,
    private val onDragEndListener: CommonAsyncListener<Any>
) : Marker.OnMarkerDragListener {

    private val oldPoint = dot.position

    override fun onMarkerDrag(marker: Marker) {
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val newPoint = marker.position
        stepManager.modifyElement(oldPoint, newPoint)
        onDragEndListener.doSomething(Any())
    }

    override fun onMarkerDragStart(marker: Marker) {
        MapIWOverlayUtils.clearInfoWindows(graphicOverlay)
    }
}