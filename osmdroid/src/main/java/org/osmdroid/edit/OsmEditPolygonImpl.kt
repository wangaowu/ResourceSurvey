package org.osmdroid.edit

import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.util.Log
import com.bytemiracle.base.framework.component.BaseActivity
import com.bytemiracle.base.framework.listener.CommonAsyncListener
import com.bytemiracle.base.framework.utils.XToastUtils
import com.bytemiracle.base.framework.utils.common.ListUtils
import mil.nga.geopackage.GeoPackage
import mil.nga.geopackage.features.user.FeatureRow
import mil.nga.geopackage.geom.GeoPackageGeometryData
import mil.nga.sf.LineString
import mil.nga.sf.Point
import org.jts.CoordinateTransform
import org.jts.utils.JTSUtils
import org.jts.utils.MCollections
import org.osmdroid.customImpl.geopackage.GeoPackageQuick
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.base.listener.DotClickDeleteListener
import org.osmdroid.edit.base.listener.DotDraggingListener
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.edit.bean.GraphicMode
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.overlay.bean.FeatureOverlayInfo
import org.osmdroid.overlay.bean.options.OsmRenderOption
import org.osmdroid.overlay.render.IWPolygon
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.overlay.utils.MapIWOverlayUtils
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import java.util.function.Function
import java.util.function.Predicate
import java.util.stream.Collectors


/**
 * 类功能：多边形编辑的实现
 *
 * @author gwwang
 * @date 2022/2/8 14:06
 */
class OsmEditPolygonImpl(
    val mapView: MapView,
    val osmRenderOption: OsmRenderOption,
    val overlayName: String
) : OsmEditPolygonStepManager(mapView, overlayName) {

    companion object {
        const val TAG = "OsmEditPolygonImpl"
    }

    //正在识别的feature图形
    private var identifyFeature: IWPolygon? = null

    //绘制时需要监听事件的图层
    private val drawPolygonEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {

        override fun singleTapConfirmedHelper(tapPoint: GeoPoint?): Boolean {
            tapPoint?.let {
                //清除已有的infoWindow弹窗
                if (MapIWOverlayUtils.isShowInfoWindow(editGraphicOverlay)) {
                    MapIWOverlayUtils.clearInfoWindows(editGraphicOverlay)
                    return false
                }
                when (graphicMode) {
                    //创建洞岛
                    GraphicMode.CREATE_UNION_HOLE -> {
                        if (JTSUtils.isInnerPoint(tapPoint, outRingStepManager.pts)) {
                            val appendIndex = createHoleStepManager.pts.size
                            createHoleStepManager.insertElement(
                                appendIndex,
                                tapPoint
                            )
                            drawDotPolygonWithHole()
                        } else {
                            XToastUtils.info("新增孔顶点必须在图形内部!")
                        }
                        return true
                    }
                    //创建分割线
                    GraphicMode.DIVIDE_POLYGON -> {
                        val appendIndex = dividerLineStepManager.pts.size
                        dividerLineStepManager.insertElement(
                            appendIndex,
                            tapPoint
                        )
                        drawDotPolygonWithHole()
                        return true
                    }
                    else -> {
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
                            val geoPoints = outRingStepManager.pts
                            if (!JTSUtils.isInnerPoint(tapPoint, geoPoints)) {
                                val suitableIndex =
                                    JTSUtils.getSuitableIndexBySimpleGeometry(geoPoints, tapPoint)
                                outRingStepManager.insertElement(suitableIndex, tapPoint)
                                drawDotPolygonWithHole()
                            } else {
                                XToastUtils.info("新增点不能在图形内部!")
                            }
                            true
                        }
                    }
                }
            }
            return false
        }

        override fun longPressHelper(p: GeoPoint?): Boolean {
            return false
        }
    })

    /**
     * 切换编辑模式
     */
    open fun switchMode(mode: GraphicMode) {
        this.graphicMode = mode
        drawDotPolygonWithHole()
    }

    override fun hideOriginAndCopyFeature(identifyFeature: IWPolygon) {
        this.identifyFeature = identifyFeature
        this.graphicMode = GraphicMode.UD_POINT

        //1、清除graphic原来信息
        clearExistGraphicInfo()
        //2.构造操作步骤管理器
        if (!ListUtils.isEmpty(identifyFeature.holes)) {
            val holes = MCollections.deepCopyPointsList(identifyFeature.holes)
            for (hole in holes) {
                holeStepManagers.add(EditStepManager.newInstance(hole.toMutableList()))
            }
        }
        outRingStepManager.clearAll()
        outRingStepManager.pts.addAll(MCollections.deepCopyPoints(identifyFeature.actualPoints))
        //3.hide origin
        identifyFeature.isEnabled = false
        mapView.invalidate()
        //4.copy new geometry
        assertGraphicOverlay()
        drawDotPolygonWithHole()
    }

    /**
     * 撤销切割的面
     */
    open fun undoDividePolygons() {
        val deletedHoleLevelStepCaches = undoHoleLevelStepCaches.stream()
            .filter { it.action == EditStepManager.Action.DELETE }
            .collect(Collectors.toList())
        if (deletedHoleLevelStepCaches.isNotEmpty()) {
            val deletedHoleLevelSteps = deletedHoleLevelStepCaches.stream()
                .map { it.stepManager }
                .collect(Collectors.toList())
            holeStepManagers.addAll(deletedHoleLevelSteps)
            undoHoleLevelStepCaches.removeAll(deletedHoleLevelStepCaches)
        }
        switchMode(GraphicMode.UD_POINT)
        mapView.invalidate()
    }

    /**
     * 撤销删除的孔
     */
    open fun undoDeletedHoles() {
        val deletedHoleLevelStepCaches = undoHoleLevelStepCaches.stream()
            .filter { it.action == EditStepManager.Action.DELETE }
            .collect(Collectors.toList())
        if (deletedHoleLevelStepCaches.isNotEmpty()) {
            val deletedHoleLevelSteps = deletedHoleLevelStepCaches.stream()
                .map { it.stepManager }
                .collect(Collectors.toList())
            holeStepManagers.addAll(deletedHoleLevelSteps)
            undoHoleLevelStepCaches.removeAll(deletedHoleLevelStepCaches)
        }
        switchMode(GraphicMode.UD_POINT)
        mapView.invalidate()
    }

    /**
     * 撤销新增的孔
     */
    open fun undoCreatedHoles() {
        createHoleStepManager.clearAll()
        switchMode(GraphicMode.UD_POINT)
        mapView.invalidate()
    }

    override fun restoreCopyFeature() {
        //1.移除本图层、事件图层
        clearExistGraphicInfo()
        //2.恢复原有图层上的图形
        this.identifyFeature?.isEnabled = true
        mapView.invalidate()
    }

    override fun getDrawGraphicInfo(): HolePolygonInfo {
        ensureCreateHoleMerged()
        val ringPts = MCollections.ensureClosedPoints(outRingStepManager.pts)
        val holes: MutableList<MutableList<GeoPoint>> =
            mutableListOf(MCollections.ensureClosedPoints(createHoleStepManager.pts))
        holeStepManagers.map { MCollections.ensureClosedPoints(it.pts) }.toCollection(holes)
        return HolePolygonInfo(ringPts, holes, dividerLineStepManager.pts)
    }

    override fun prepareSaveEditGeometryResult(
        activity: BaseActivity,
        overlayInfo: FeatureOverlayInfo,
        onSaveSuccessListener: CommonAsyncListener<Boolean>
    ) {
        val polygonInfo = getDrawGraphicInfo()
        if (ListUtils.isEmpty(polygonInfo.ringPts) || polygonInfo.ringPts.size < 4) {
            XToastUtils.info("请检查多边形顶点数不少于3个!")
            return
        }
        //判断有线分割
        if (!ListUtils.isEmpty(polygonInfo.divideLinePts) && polygonInfo.divideLinePts.size >= 2) {
            val jtsPolygons = JTSUtils.divideGeometry(
                polygonInfo.ringPts,
                polygonInfo.holes,
                polygonInfo.divideLinePts
            )
            val osmPolygons = CoordinateTransform.toSfPolygons(jtsPolygons)
            val featureRows = differFeatureRows(osmPolygons, overlayInfo.featureRow)
            executeSaveEditGeometryResult2(
                featureRows,
                overlayInfo.packageOverlay,
                onSaveSuccessListener
            )
            return
        }
        if (!hasInvalidHoles(polygonInfo.holes)) {
            val featureRows = buildFeatureRows(polygonInfo, overlayInfo)
            executeSaveEditGeometryResult2(
                featureRows,
                overlayInfo.packageOverlay,
                onSaveSuccessListener
            )
        } else {
            AlertDialog.Builder(activity).setTitle("提示").setMessage("自动忽略无效孔洞数据?")
                .setPositiveButton(
                    "忽略"
                ) { dialog, _ ->
                    val featureRows = buildFeatureRows(polygonInfo, overlayInfo)
                    executeSaveEditGeometryResult2(
                        featureRows,
                        overlayInfo.packageOverlay,
                        onSaveSuccessListener
                    )
                    dialog.dismiss()
                }.show();
        }
    }

    //执行保存
    private fun executeSaveEditGeometryResult2(
        featureRows: Array<FeatureRow>,
        packageOverlay: PackageOverlay,
        onSaveSuccessListener: CommonAsyncListener<Boolean>
    ) {
        //所属图层信息-->所属表
        //1.更新行属性
        packageOverlay.packageOverlayInfo.openWritableGeoPackage { geoPackage: GeoPackage ->
            val featureDao = geoPackage.getFeatureDao(packageOverlay.name)
            //2.插入or更新表
            for (featureRow in featureRows) {
                if (featureRow.hasId()) {
                    //更新
                    featureDao.update(featureRow)
                } else {
                    //新增
                    featureDao.insert(featureRow)
                }
            }
            GeoPackageQuick.sink2Database(geoPackage)
            geoPackage.close()
            //3.清空编辑过程中的内容
            clearExistGraphicInfo()
            //4.更新完成之后刷新本图层的所有要素
            onSaveSuccessListener.doSomething(true)
        }
    }

    private fun buildFeatureRows(
        polygonInfo: HolePolygonInfo,
        overlayInfo: FeatureOverlayInfo
    ): Array<FeatureRow> {
        val sfGeometry = buildSfGeometry(polygonInfo)
        overlayInfo.featureRow.geometry = GeoPackageGeometryData(sfGeometry)
        return arrayOf(overlayInfo.featureRow)
    }

    private fun differFeatureRows(
        sfGeometries: List<mil.nga.sf.Polygon>,
        fr: FeatureRow
    ): Array<FeatureRow> {
        val copyFeatureRows = sfGeometries.map { fr.copy() }.toList()
        copyFeatureRows.forEachIndexed { index, cfr ->
            if (index == 0) {
                //拆分时，第一个更新行属性
                cfr.geometry = GeoPackageGeometryData(sfGeometries[index])
            } else {
                //拆分时，剩余为新增行属性
                cfr.geometry = GeoPackageGeometryData(sfGeometries[index])
                cfr.values[cfr.pkColumnIndex] = null
            }
        }
        return copyFeatureRows.toTypedArray()
    }

    private fun buildSfGeometry(polygonInfo: HolePolygonInfo): mil.nga.sf.Polygon {
        //构建新图形数据
        val ringPoints: List<Point> = polygonInfo.ringPts.stream()
            .map(Function { geoPoint: GeoPoint ->
                Point(
                    geoPoint.longitude,
                    geoPoint.latitude
                )
            })
            .collect(Collectors.toList())
        val holesPoints: List<List<Point?>> = polygonInfo.holes.stream()
            .filter(Predicate { geoPoints: List<GeoPoint?> -> geoPoints.size >= 4 })
            .map(Function { geoPoints: List<GeoPoint> ->
                geoPoints.stream()
                    .map { geoPoint: GeoPoint ->
                        Point(
                            geoPoint.longitude,
                            geoPoint.latitude
                        )
                    }
                    .collect(Collectors.toList())
            })
            .collect(Collectors.toList())
        val sfGeometry = mil.nga.sf.Polygon(LineString(ringPoints))
        for (holesPoint in holesPoints) {
            sfGeometry.addRing(LineString(holesPoint))
        }
        return sfGeometry
    }


    override fun clearExistGraphicInfo() {
        outRingStepManager.clearAll()
        createHoleStepManager.clearAll()
        dividerLineStepManager.clearAll()
        undoHoleLevelStepCaches.clear()
        redoHoleLevelStepCaches.clear()
        holeStepManagers.clear()
        MapIWOverlayUtils.clearInfoWindows(editGraphicOverlay)
        editGraphicOverlay.items.clear()
        mapView.overlayManager.remove(editGraphicOverlay)
        mapView.overlayManager.remove(drawPolygonEventsOverlay)
        mapView.invalidate()
    }

    //是否有无效的孔洞
    private fun hasInvalidHoles(holesPts: List<List<GeoPoint?>>): Boolean {
        for (holePts in holesPts) {
            if (!ListUtils.isEmpty(holePts) && holePts.size < 4) {
                return true
            }
        }
        return false
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
                .anyMatch { it === drawPolygonEventsOverlay }
        ) {
            mapView.overlayManager.add(drawPolygonEventsOverlay)
        }
    }

    override fun drawDotPolygonWithHole() {
        editGraphicOverlay.items.clear()

        //画面
        var outRing: List<GeoPoint> = outRingStepManager.pts
        var holes: MutableList<MutableList<GeoPoint>> = mutableListOf(createHoleStepManager.pts)
        holeStepManagers.map { it.pts }.toCollection(holes)
        var polygonWithHole = buildPolygonWithHole(outRing, holes)
        polygonWithHole?.let { editGraphicOverlay.add(polygonWithHole) }

        when (graphicMode) {
            //编辑、删除、创建外围顶点
            GraphicMode.UD_POINT -> {
                //画外圈线
                var outLine = buildPolyLine(outRingStepManager)
                outLine?.let { editGraphicOverlay.add(outLine) }
                //画洞岛线
                holeStepManagers.filter { it.pts.size > 1 }.forEach {
                    var holesLine = buildPolyLine(it)
                    holesLine?.let { editGraphicOverlay.add(holesLine) }
                }
                //画点
                addDotGraphicOverlay(outRingStepManager)
                //画洞岛点
                holeStepManagers.forEach {
                    addDotGraphicOverlay(it)
                }
                mapView.invalidate()
            }
            //画洞岛删除点
            GraphicMode.DELETE_HOLE -> {
                holeStepManagers.forEach {
                    addHolesUseDeleteIndicator(it)
                }
                mapView.invalidate()
            }
            //创建或合并洞岛
            GraphicMode.CREATE_UNION_HOLE -> {
                //创建洞岛的线
                buildPolyLine(createHoleStepManager)?.let { editGraphicOverlay.add(it) }
                //创建洞岛的点
                addDotGraphicOverlay(createHoleStepManager)
                mapView.invalidate()
            }
            //线分割
            GraphicMode.DIVIDE_POLYGON -> {
                //创建分割线
                buildPolyLine(dividerLineStepManager)?.let { editGraphicOverlay.add(it) }
                //创建分割线的点
                addDotGraphicOverlay(dividerLineStepManager)
                mapView.invalidate()
            }
            GraphicMode.FINAL_GRAPHIC -> {
                //do nothing
                mapView.invalidate()
            }
        }
    }

    //将图形内所有的holes合并
    private fun ensureCreateHoleMerged() {
        if (createHoleStepManager.pts.size >= 3) {
            MCollections.ensureClosedPoints(createHoleStepManager.pts)
            if (!JTSUtils.isSelfIntersection(createHoleStepManager.pts)) {
                executeUnionHoles()
            } else {
                createHoleStepManager.clearAll()
                Log.e(TAG, "新增图形非法,已自动忽略!")
            }
        }
    }

    private fun executeUnionHoles() {
        var newHolePts = createHoleStepManager.pts
        val hasIntersectionSteps = mutableListOf<EditStepManager<GeoPoint>>()
        for (holeStepManager in holeStepManagers) {
            //寻找有交集的hole管理器
            val hasIntersection = JTSUtils.hasIntersection(newHolePts, holeStepManager.pts)
            if (hasIntersection) {
                hasIntersectionSteps.add(holeStepManager)
            }
        }
        if (!ListUtils.isEmpty(hasIntersectionSteps)) {
            for (hasIntersectionStep in hasIntersectionSteps) {
                newHolePts =
                    JTSUtils.unionGeometries(newHolePts, hasIntersectionStep.pts).toMutableList()
            }
            //修改创建hole的边界
            createHoleStepManager.clearAll()
            createHoleStepManager.pts.addAll(newHolePts)
            //移除有交集的原有hole管理器
            holeStepManagers.removeAll(hasIntersectionSteps)
        }
    }

    private fun addHolesUseDeleteIndicator(stepManager: EditStepManager<GeoPoint>) {
        val first = stepManager.pts.first()
        val dotMarker = buildDot(first, deleteHoleDrawable)
        dotMarker.setOnMarkerClickListener { _, _ ->
            holeStepManagers.remove(stepManager)
            undoHoleLevelStepCaches.add(
                HoleLevelStepCache(
                    EditStepManager.Action.DELETE,
                    stepManager
                )
            )
            drawDotPolygonWithHole()
            true
        }
        editGraphicOverlay.add(dotMarker)
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
            ) { drawDotPolygonWithHole() })
            dotMarker.setOnMarkerClickListener(DotClickDeleteListener(
                dotMarker,
                stepManager,
                editGraphicOverlay
            ) { drawDotPolygonWithHole() })
            dotMarker.setInfoWindow(MarkerInfoWindow(R.layout.bonuspack_bubble, mapView))
            editGraphicOverlay.add(dotMarker)
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

    private fun buildPolyLineByChosenHole(stepManager: EditStepManager<GeoPoint>): Polyline? {
        return buildPolyLine(
            stepManager,
            osmRenderOption.polygonOption.lineWidthOnSelected,
            0xff0000ff.toInt()
        )
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

    private fun buildPolygonWithHole(
        outRing: List<GeoPoint>,
        holes: MutableList<MutableList<GeoPoint>>
    ): Polygon? {
        //无效数据
        if (outRing.size < 3) {
            return null
        }
        //有效洞岛
        val validHoles = holes.filter { it.size >= 3 }
        val polygon = Polygon()
        //外环
        polygon.points = outRing
        polygon.holes = validHoles
        polygon.outlinePaint.strokeWidth = osmRenderOption.polygonOption.lineWidthOnSelected
        polygon.outlinePaint.color = osmRenderOption.polygonOption.lineColorOnSelected
        polygon.fillPaint.color = osmRenderOption.polygonOption.fillColorOnSelected
        polygon.infoWindow = null
        return polygon
    }

    data class HolePolygonInfo(
        val ringPts: List<GeoPoint>,
        val holes: List<List<GeoPoint>>,
        val divideLinePts: List<GeoPoint>
    );
}