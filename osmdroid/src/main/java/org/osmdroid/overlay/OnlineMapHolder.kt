package org.osmdroid.overlay

import android.content.Context
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.views.overlay.TilesOverlay

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/3/12 14:35
 */
class OnlineMapHolder {

    companion object {
        private lateinit var overlays: MutableList<OnlineMap>

        fun getTileOverlays(): MutableList<OnlineMap> = overlays

        fun init(ctx: Context) {
            if (!this::overlays.isInitialized) {
                //天地图
                val tdtImgSource = TianDiTuLayer.tianDiTuImgTileSource
                val tdtCiaOverlay = TilesOverlay(
                    MapTileProviderBasic(ctx, TianDiTuLayer.tianDiTuCiaTileSource),
                    ctx
                )
                //高德
                val aMapImgSource = OtherLayer.aMapVector
                //腾讯
                val tencentImgSource = OtherLayer.tencentVector

                overlays = mutableListOf(
                    OnlineMap(
                        tdtImgSource,
                        tdtCiaOverlay,
                        visible = true,
                        showLabel = true
                    ),
                    OnlineMap(
                        aMapImgSource,
                        null,
                        visible = false,
                        showLabel = false
                    ),
                    OnlineMap(
                        tencentImgSource,
                        null,
                        visible = false,
                        showLabel = false
                    )
                )
            }
        }
    }

    data class OnlineMap(
        val imgSource: ITileSource,
        val labelOverlay: TilesOverlay?,
        var visible: Boolean,
        var showLabel: Boolean
    )
}