package org.osmdroid.edit

import android.graphics.drawable.Drawable
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.bean.GraphicMode
import org.osmdroid.overlay.render.PackageOverlay
import org.osmdroid.views.MapView

/**
 * 类功能：基础绘制
 *
 * @author gwwang
 * @date 2022/5/5 15:14
 */
open class OsmEditGraphicOverlay(mapView: MapView, overlayName: String) {

    //编辑点符号
    open val editPointDrawable: Drawable =
        mapView.context.resources.getDrawable(R.drawable.rect_edit_point)
    open val editPointWidth = editPointDrawable.bounds.width()

    //删除点符号
    open val deleteHoleDrawable: Drawable =
        mapView.context.resources.getDrawable(R.drawable.circle_delete_hole)

    //绘制图形的图层
    open val editGraphicOverlay = PackageOverlay(overlayName)

    //绘制方法
    open fun drawDotPolygonWithHole() {}
}