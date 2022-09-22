package org.osmdroid.edit

import android.graphics.drawable.Drawable
import android.util.Log
import com.bytemiracle.base.framework.component.BaseActivity
import com.bytemiracle.base.framework.listener.CommonAsyncListener
import com.bytemiracle.base.framework.utils.XToastUtils
import com.bytemiracle.base.framework.utils.common.ListUtils
import mil.nga.geopackage.GeoPackage
import mil.nga.geopackage.geom.GeoPackageGeometryData
import mil.nga.sf.LineString
import mil.nga.sf.Point
import mil.nga.sf.wkt.GeometryWriter
import org.jts.utils.JTSUtils
import org.osmdroid.customImpl.geopackage.GeoPackageQuick
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.base.IEditFeature
import org.osmdroid.edit.base.listener.DotClickDeleteListener
import org.osmdroid.edit.base.listener.DotDraggingListener
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.bean.FeatureOverlayInfo
import org.osmdroid.overlay.bean.options.OsmRenderOption
import org.osmdroid.overlay.render.IWPolyline
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.overlay.utils.MapIWOverlayUtils
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.io.IOException

/**
 * 类功能：线条编辑的实现
 *
 * @author gwwang
 * @date 2022/5/23 0023 15:32
 */
class OsmEditLinestringImpl(
    val mapView: MapView,
    val osmRenderOption: OsmRenderOption,
    val overlayName: String
) : IEditFeature<IWPolyline, List<GeoPoint>> {

    companion object {
        const val TAG = "OsmEditLinestringImpl"
    }

    //编辑点符号
    private val editPointDrawable: Drawable =
        mapView.context.resources.getDrawable(R.drawable.rect_edit_point)
    private val editPointWidth = editPointDrawable.bounds.width()

    //绘制图形的图层
    private val editGraphicOverlay = PackageOverlay(overlayName)

    private var identifyFeature: IWPolyline? = null
    private val editStepManager = EditStepManager.newInstance2<GeoPoint>(mutableListOf())

    //绘制时需要监听事件的图层
    private val drawPointEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {

        override fun singleTapConfirmedHelper(tapPoint: GeoPoint?): Boolean {
            tapPoint?.let {
                //清除已有的infoWindow弹窗
                if (MapIWOverlayUtils.isShowInfoWindow(editGraphicOverlay)) {
                    MapIWOverlayUtils.clearInfoWindows(editGraphicOverlay)
                    return false
                }
                //图层的绘制事件(新增点)，会与已有点的showWindowInfo事件冲突，所以必须判定点击不要在点的范围上
                val tapedMarker = JTSUtils.isTapOnMarker(
                    tapPoint,
                    editPointWidth,
                    editGraphicOverlay,
                    mapView
                )
                if (tapedMarker) {
                    //点击到了点上，响应点的showWindowInfo事件
                    false
                } else {
                    val geoPoints = editStepManager.pts
                    if (!JTSUtils.isLinestringPoint(tapPoint, geoPoints)) {
                        editStepManager.insertElement(geoPoints.size, tapPoint)
                        drawLinestring()
                    } else {
                        XToastUtils.info("新增点不能在线段上!")
                    }
                    true
                }
            }
            return false
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    override fun hideOriginAndCopyFeature(identifyFeature: IWPolyline) {
        this.identifyFeature = identifyFeature
        //1、清除graphic原来信息
        clearExistGraphicInfo()
        //2.构造操作步骤管理器
        editStepManager.clearAll()
        editStepManager.pts.addAll(identifyFeature.actualPoints)
        //3.hide origin
        identifyFeature.isEnabled = false
        mapView.invalidate()
        //4.copy new geometry
        assertGraphicOverlay()
        drawLinestring()
    }

    private fun assertGraphicOverlay() {
        //绘制图层
        if (!mapView.overlayManager.overlays().stream()
                .anyMatch { it === editGraphicOverlay }
        ) {
            mapView.overlayManager.add(editGraphicOverlay)
        }
        //事件图层
        if (!mapView.overlayManager.overlays().stream()
                .anyMatch { it === drawPointEventsOverlay }
        ) {
            mapView.overlayManager.add(drawPointEventsOverlay)
        }
    }

    fun drawLinestring() {
        editGraphicOverlay.items.clear()
        //画线
        var linestrings = buildPolyLine(editStepManager)
        linestrings?.let { editGraphicOverlay.add(linestrings) }
        //画点
        addDotGraphicOverlay(editStepManager)
        mapView.invalidate()
    }

    private fun addDotGraphicOverlay(stepManager: EditStepManager<GeoPoint>) {
        stepManager.pts.forEach {
            val dotMarker = buildDot(it, editPointDrawable)
            dotMarker.isDraggable = true
            dotMarker.dragOffset = 8f //拖拽时，标记点跳离手指高度（避免在指中心看不见导致不直观）
            dotMarker.setOnMarkerDragListener(DotDraggingListener(
                dotMarker,
                stepManager,
                editGraphicOverlay
            ) { drawLinestring() })
            dotMarker.setOnMarkerClickListener(DotClickDeleteListener(
                dotMarker,
                stepManager,
                editGraphicOverlay
            ) { drawLinestring() })
            dotMarker.setInfoWindow(MarkerInfoWindow(R.layout.bonuspack_bubble, mapView))
            editGraphicOverlay.add(dotMarker)
        }
    }

    private fun buildPolyLine(stepManager: EditStepManager<GeoPoint>): Polyline? {
        return buildPolyLine(
            stepManager,
            osmRenderOption.polygonOption.lineWidthOnSelected,
            osmRenderOption.polygonOption.lineColorOnSelected
        )
    }

    private fun buildPolyLine(
        stepManager: EditStepManager<GeoPoint>,
        lineWidth: Float,
        lineColor: Int
    ): Polyline? {
        val pts = stepManager.pts
        if (pts.size > 1) {
            val polyline = Polyline()
            polyline.setPoints(pts)
            polyline.outlinePaint.strokeWidth = lineWidth
            polyline.outlinePaint.color = lineColor
            polyline.infoWindow = null
            return polyline
        }
        return null
    }

    private fun buildDot(p: GeoPoint, pointDrawable: Drawable): Marker {
        val dot = Marker(mapView)
        dot.position = p
        dot.icon = pointDrawable
        dot.setAnchor(.5f, .5f) //偏移的是BitmapBounds宽度的倍数
        dot.infoWindow = null
        return dot
    }

    override fun restoreCopyFeature() {
        //1.移除本图层、事件图层
        clearExistGraphicInfo()
        //2.恢复原有图层上的图形
        this.identifyFeature?.isEnabled = true
        mapView.invalidate()
    }

    override fun undoCachedStep() {
        if (editStepManager.undoCachedStep()) {
            drawLinestring()
        } else {
            XToastUtils.info("没有可以撤销的操作")
        }
    }

    override fun redoCachedStep() {
        if (editStepManager.redoCachedStep()) {
            drawLinestring()
        } else {
            XToastUtils.info("没有可以恢复的操作")
        }
    }

    override fun clearExistGraphicInfo() {
        editStepManager.clearAll()
        editGraphicOverlay.items.clear()
        mapView.overlayManager.remove(editGraphicOverlay)
        mapView.overlayManager.remove(drawPointEventsOverlay)
        mapView.invalidate()
    }

    override fun getDrawGraphicInfo(): List<GeoPoint> {
        return editStepManager.pts
    }

    override fun prepareSaveEditGeometryResult(
        activity: BaseActivity,
        overlayInfo: FeatureOverlayInfo,
        onSaveSuccessListener: CommonAsyncListener<Boolean>
    ) {
        val linestringPts = getDrawGraphicInfo()
        if (ListUtils.isEmpty(linestringPts) || linestringPts.size < 2) {
            XToastUtils.info("线数据异常,请重新编辑!")
            return
        }
        //构建新图形数据
        val sfGeometry = LineString(linestringPts.map { Point(it.longitude, it.latitude) })
        try {
            val wkt = GeometryWriter.writeGeometry(sfGeometry)
            Log.e("AAAA", "saveEditGeometryResult: $wkt\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //所属图层信息-->所属表
        val packageOverlay = overlayInfo.packageOverlay
        val tableName = packageOverlay.name
        //1.更新行属性
        val featureRow = overlayInfo.featureRow
        val newGeometryData = GeoPackageGeometryData(sfGeometry)
        featureRow.geometry = newGeometryData
        packageOverlay.packageOverlayInfo.openWritableGeoPackage { geoPackage: GeoPackage ->
            val featureDao = geoPackage.getFeatureDao(tableName)
            //2.更新表
            if (featureDao.update(featureRow) > 0) {
                GeoPackageQuick.sink2Database(geoPackage)
                geoPackage.close()
                //3.清空编辑过程中的内容
                clearExistGraphicInfo()
                //4.更新完成之后刷新本图层的所有要素
                onSaveSuccessListener.doSomething(true)
            } else {
                onSaveSuccessListener.doSomething(false)
            }
        }
    }
}