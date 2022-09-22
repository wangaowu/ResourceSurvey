package org.osmdroid.edit

import android.graphics.drawable.Drawable
import android.util.Log
import com.bytemiracle.base.framework.component.BaseActivity
import com.bytemiracle.base.framework.listener.CommonAsyncListener
import com.bytemiracle.base.framework.utils.XToastUtils
import mil.nga.geopackage.GeoPackage
import mil.nga.geopackage.geom.GeoPackageGeometryData
import mil.nga.sf.Point
import mil.nga.sf.wkt.GeometryWriter
import org.osmdroid.customImpl.geopackage.GeoPackageQuick
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.base.IEditFeature
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.bean.FeatureOverlayInfo
import org.osmdroid.overlay.bean.options.OsmRenderOption
import org.osmdroid.overlay.render.IWMarker
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.io.IOException

/**
 * 类功能：点编辑的实现
 *
 * @author gwwang
 * @date 2022/5/23 0023 15:32
 */
class OsmEditPointImpl(
    val mapView: MapView,
    val osmRenderOption: OsmRenderOption,
    val overlayName: String
) : IEditFeature<IWMarker, GeoPoint> {

    companion object {
        const val TAG = "OsmEditPointImpl"
    }

    //编辑点符号
    private val editPointDrawable: Drawable =
        mapView.context.resources.getDrawable(R.drawable.rect_edit_point)

    //绘制图形的图层
    private val editGraphicOverlay = PackageOverlay(overlayName)

    private var identifyFeature: IWMarker? = null
    private val editStepManager = EditStepManager.newInstance2<GeoPoint>(mutableListOf())

    //绘制时需要监听事件的图层
    private val drawPointEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {

        override fun singleTapConfirmedHelper(tapPoint: GeoPoint?): Boolean {
            tapPoint?.let {
                val srcGeoPoint = editStepManager.pts.first()
                editStepManager.modifyElement(srcGeoPoint, tapPoint)
                drawDot()
                true
            }
            return false
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    override fun hideOriginAndCopyFeature(identifyFeature: IWMarker) {
        this.identifyFeature = identifyFeature
        //1、清除graphic原来信息
        clearExistGraphicInfo()
        //2.构造操作步骤管理器
        editStepManager.clearAll()
        editStepManager.pts.add(identifyFeature.position)
        //3.hide origin
        identifyFeature.isEnabled = false
        mapView.invalidate()
        //4.copy new geometry
        assertGraphicOverlay()
        drawDot()
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

    fun drawDot() {
        editGraphicOverlay.items.clear()
        //画点
        val point = editStepManager.pts.first()
        point?.let {
            editGraphicOverlay.add(buildDot(it, editPointDrawable))
            mapView.invalidate()
        }
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
            drawDot()
        } else {
            XToastUtils.info("没有可以撤销的操作")
        }
    }

    override fun redoCachedStep() {
        if (editStepManager.redoCachedStep()) {
            drawDot()
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

    override fun getDrawGraphicInfo(): GeoPoint {
        return editStepManager.pts.first()
    }

    override fun prepareSaveEditGeometryResult(
        activity: BaseActivity,
        overlayInfo: FeatureOverlayInfo,
        onSaveSuccessListener: CommonAsyncListener<Boolean>
    ) {
        val modifiedGeoPoint = getDrawGraphicInfo()
        if (modifiedGeoPoint == null) {
            XToastUtils.info("点数据异常,请重新编辑!")
            return
        }
        //构建新图形数据
        val sfGeometry = Point(modifiedGeoPoint.longitude, modifiedGeoPoint.latitude)
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