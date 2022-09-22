package org.osmdroid.edit.base.listener

import android.widget.TextView
import com.bytemiracle.base.framework.listener.CommonAsyncListener
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.overlay.utils.MapIWOverlayUtils
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

/**
 * 类功能：点击点的删除事件
 *
 * @author gwwang
 * @date 2022/5/5 16:42
 */
open class DotClickDeleteListener(
    val dot: Marker,
    private val stepManager: EditStepManager<GeoPoint>,
    private val graphicOverlay: PackageOverlay,
    private val onDeleteListener: CommonAsyncListener<Any>
) : Marker.OnMarkerClickListener {
    override fun onMarkerClick(marker: Marker, mapView: MapView): Boolean {
        val infoWindow = dot.infoWindow
        if ((infoWindow is MarkerInfoWindow) && stepManager.pts.size > 1) {
            MapIWOverlayUtils.showSingleInfoWindow(graphicOverlay, marker)
            //infoWindow的所有信息，必须在shown之后，调用invalidate才会有用
            setViewListenersOnShown(infoWindow)
        }
        return true
    }

    private fun setViewListenersOnShown(infoWindow: MarkerInfoWindow) {
        val tvDelete = infoWindow.view.findViewById<TextView>(R.id.bubble_title)
        tvDelete.setOnClickListener {
            MapIWOverlayUtils.clearInfoWindows(graphicOverlay)
            stepManager.removeSpecialElement(dot.position)
            onDeleteListener.doSomething(Any())
        }
        tvDelete.text = "点击删除"
        infoWindow.view.invalidate()
    }
}