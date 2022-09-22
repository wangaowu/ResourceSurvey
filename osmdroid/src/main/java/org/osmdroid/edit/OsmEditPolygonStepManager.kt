package org.osmdroid.edit

import com.bytemiracle.base.framework.utils.XToastUtils
import com.bytemiracle.base.framework.utils.common.ListUtils
import org.osmdroid.edit.base.IEditFeature
import org.osmdroid.edit.bean.EditStepManager
import org.osmdroid.edit.bean.GraphicMode
import org.osmdroid.overlay.render.IWPolygon
import org.osmdroid.overlay.utils.MapIWOverlayUtils
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * 类功能：步骤业务类
 *
 * @author gwwang
 * @date 2022/5/5 13:49
 */
open abstract class OsmEditPolygonStepManager(
    mapView: MapView,
    overlayName: String
) : OsmEditGraphicOverlay(mapView, overlayName),
    IEditFeature<IWPolygon, OsmEditPolygonImpl.HolePolygonInfo> {

    //图形编辑模式
    open var graphicMode: GraphicMode = GraphicMode.FINAL_GRAPHIC

    //图形外圈点步骤管理器
    open val outRingStepManager: EditStepManager<GeoPoint> = EditStepManager.newInstance(
        mutableListOf()
    )

    //图形内圈点步骤管理器
    open val holeStepManagers: MutableList<EditStepManager<GeoPoint>> = mutableListOf()

    //孔级别的步骤管理器
    open val undoHoleLevelStepCaches: MutableList<HoleLevelStepCache> =
        mutableListOf()
    open val redoHoleLevelStepCaches: MutableList<HoleLevelStepCache> =
        mutableListOf()

    //创建孔的点步骤管理器
    open var createHoleStepManager: EditStepManager<GeoPoint> =
        EditStepManager.newInstance(mutableListOf())

    //创建分割线的步骤管理器
    open var dividerLineStepManager: EditStepManager<GeoPoint> =
        EditStepManager.newInstance(mutableListOf())

    override fun undoCachedStep() {
        if (graphicMode == GraphicMode.DIVIDE_POLYGON &&
            dividerLineStepManager.undoCachedStep()
        ) {
            drawDotPolygonWithHole()
            return
        }
        if (graphicMode == GraphicMode.CREATE_UNION_HOLE &&
            createHoleStepManager.undoCachedStep()
        ) {
            drawDotPolygonWithHole()
            return
        }
        if (consumeUndoSteps()) {
            MapIWOverlayUtils.clearInfoWindows(editGraphicOverlay)
            drawDotPolygonWithHole()
        } else {
            XToastUtils.info("没有可以撤销的操作!")
        }
    }

    override fun redoCachedStep() {
        if (graphicMode == GraphicMode.DIVIDE_POLYGON &&
            dividerLineStepManager.redoCachedStep()
        ) {
            drawDotPolygonWithHole()
            return
        }
        if (graphicMode == GraphicMode.CREATE_UNION_HOLE &&
            createHoleStepManager.redoCachedStep()
        ) {
            drawDotPolygonWithHole()
            return
        }
        if (consumeRedoSteps()) {
            MapIWOverlayUtils.clearInfoWindows(editGraphicOverlay)
            drawDotPolygonWithHole()
        } else {
            XToastUtils.info("没有可以恢复的操作!")
        }
    }

    private fun consumeUndoSteps(): Boolean {
        if (!consumeWholeUndoSteps()) {
            val managers = mutableListOf(outRingStepManager)
            managers.addAll(holeStepManagers)
            for (manager in managers) {
                if (manager.undoCachedStep()) {
                    return true
                }
            }
        }
        return false
    }

    private fun consumeRedoSteps(): Boolean {
        if (!consumeWholeRedoSteps()) {
            val managers = mutableListOf(outRingStepManager)
            managers.addAll(holeStepManagers)
            for (manager in managers) {
                if (manager.redoCachedStep()) {
                    return true
                }
            }
        }
        return false
    }

    private fun consumeWholeUndoSteps(): Boolean {
        if (!ListUtils.isEmpty(undoHoleLevelStepCaches)) {
            val lastStep = undoHoleLevelStepCaches.last()
            when (lastStep.action) {
                EditStepManager.Action.DELETE -> {
                    holeStepManagers.add(lastStep.stepManager)
                }
                EditStepManager.Action.ADD -> {
                    holeStepManagers.remove(lastStep.stepManager)
                }
            }
            drawDotPolygonWithHole()
            redoHoleLevelStepCaches.add(lastStep)
            return true
        }
        return false
    }

    private fun consumeWholeRedoSteps(): Boolean {
        if (!ListUtils.isEmpty(redoHoleLevelStepCaches)) {
            val lastStep = redoHoleLevelStepCaches.last()
            when (lastStep.action) {
                EditStepManager.Action.DELETE -> {
                    holeStepManagers.remove(lastStep.stepManager)
                }
                EditStepManager.Action.ADD -> {
                    holeStepManagers.add(lastStep.stepManager)
                }
            }
            drawDotPolygonWithHole()
            undoHoleLevelStepCaches.add(lastStep)
            return true
        }
        return false
    }

    data class HoleLevelStepCache(
        val action: EditStepManager.Action,
        val stepManager: EditStepManager<GeoPoint>
    )
}