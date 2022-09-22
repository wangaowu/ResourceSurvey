package org.osmdroid.overlay;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.MapTileIndex;

/**
 * 类功能：高德地图
 *
 * @author gwwang
 * @date 2022/3/12 11:04
 */
public class OtherLayer {

    //高德地图
    public static final OnlineTileSourceBase aMapVector = new XYTileSource("Amap-Vector",
            0, 20, 256, ".png", new String[]{
            "https://wprd01.is.autonavi.com/appmaptile?",
            "https://wprd02.is.autonavi.com/appmaptile?",
            "https://wprd03.is.autonavi.com/appmaptile?",
            "https://wprd04.is.autonavi.com/appmaptile?",
    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "x=" + MapTileIndex.getX(pMapTileIndex) + "&y=" + MapTileIndex.getY(pMapTileIndex) + "&z="
                    + MapTileIndex.getZoom(pMapTileIndex) + "&lang=zh_cn&size=1&scl=1&style=7&ltype=7";
        }
    };

    //腾讯地图
    public static final OnlineTileSourceBase tencentVector = new XYTileSource("tencent-Vector",
            0, 20, 256, ".png", new String[]{
            "http://rt0.map.gtimg.com/realtimerender?"
    }) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            return getBaseUrl() + "z= " + MapTileIndex.getZoom(pMapTileIndex) +
                    "&x=" + MapTileIndex.getX(pMapTileIndex) +
                    "&y=" + (-MapTileIndex.getY(pMapTileIndex)) + "&type=vector&style=0";
        }
    };
}
