package org.osmdroid.edit.base

import com.bytemiracle.base.framework.component.BaseActivity
import com.bytemiracle.base.framework.listener.CommonAsyncListener
import org.osmdroid.edit.OsmEditPolygonImpl
import org.osmdroid.overlay.bean.FeatureOverlayInfo
import org.osmdroid.views.overlay.OverlayWithIW

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/23 0023 9:24
 */
interface IEditFeature<TypeOfFeature : OverlayWithIW, DataOfGraphic : Any> {

    /**
     * 隐藏原有图形并拷贝到新图层
     */
    fun hideOriginAndCopyFeature(overlayWithIW: TypeOfFeature)

    /**
     * 恢复拷贝的图形
     */
    fun restoreCopyFeature();

    /**
     * 撤销操作
     */
    fun undoCachedStep()

    /**
     * 恢复步骤
     */
    fun redoCachedStep()

    /**
     * 清空编辑过程中的所有内容
     */
    fun clearExistGraphicInfo()

    /**
     * 获取绘制的点
     */
    fun getDrawGraphicInfo(): DataOfGraphic

    /**
     * 保存编辑之后的geometry
     */
    fun prepareSaveEditGeometryResult(
        activity: BaseActivity,
        overlayInfo: FeatureOverlayInfo,
        onSaveSuccessListener: CommonAsyncListener<Boolean>
    )

    public interface OnInterceptListener {
        fun onIntercept(
            polygonInfo: OsmEditPolygonImpl.HolePolygonInfo,
            overlayInfo: FeatureOverlayInfo,
            onSaveSuccessListener: CommonAsyncListener<Boolean>
        )
    }
}