package org.osmdroid.overlay

import org.osmdroid.overlay.utils.MapConstant
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.util.MapTileIndex

/**
 * 类功能：天地图
 *
 * @author gwwang
 * @date 2022/2/17 10:36
 */
class TianDiTuLayer {
    companion object {
        const val TAG: String = "OsmMapFragment"
        const val TOKEN = "&tk=abe31c11863ba46e8937cf056569f702"

        //影像地图 _W是墨卡托投影  _c是国家2000的坐标系
        val tianDiTuImgTileSource = object : OnlineTileSourceBase(
            MapConstant.OVERLAY_TIAN_DI_TU_IMAGE, 1, 18, 256, "",
            arrayOf(
                "http://t1.tianditu.com/DataServer?T=img_w",
                "http://t2.tianditu.com/DataServer?T=img_w",
                "http://t3.tianditu.com/DataServer?T=img_w",
                "http://t4.tianditu.com/DataServer?T=img_w",
                "http://t5.tianditu.com/DataServer?T=img_w",
                "http://t6.tianditu.com/DataServer?T=img_w"
            ), null, TileSourcePolicy(0, 0)
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return "$baseUrl$TOKEN&X=${MapTileIndex.getX(pMapTileIndex)}&Y=${
                    MapTileIndex.getY(pMapTileIndex)
                }&L=${MapTileIndex.getZoom(pMapTileIndex)}"
            }
        }

        //影像标注 _W是墨卡托投影  _c是国家2000的坐标系
        val tianDiTuCiaTileSource = object : OnlineTileSourceBase(
            MapConstant.OVERLAY_TIAN_DI_TU_CIA, 1, 18, 256, "",
            arrayOf(
                "http://t1.tianditu.com/DataServer?T=cia_w",
                "http://t2.tianditu.com/DataServer?T=cia_w",
                "http://t3.tianditu.com/DataServer?T=cia_w",
                "http://t4.tianditu.com/DataServer?T=cia_w",
                "http://t5.tianditu.com/DataServer?T=cia_w",
                "http://t6.tianditu.com/DataServer?T=cia_w"
            ), null, TileSourcePolicy(0, 0)
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                return "$baseUrl$TOKEN&X=${MapTileIndex.getX(pMapTileIndex)}&Y=${
                    MapTileIndex.getY(pMapTileIndex)
                }&L=${MapTileIndex.getZoom(pMapTileIndex)}"
            }
        }

    }
}