package com.bytemiracle.resourcesurvey.common;


import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;

/**
 * 类功能：消息事件
 *
 * @author gwwang
 * @date 2021/5/27 16:42
 */
public class EventCluster {

    /**
     * 切换工程
     */
    public static class EventChangeProject {

    }

    /**
     * 全部图层加载完成的事件
     */
    public static class EventAllLayersLoaded {

    }

    /**
     * 编辑字段
     */
    public static class EventUpdateFields {


    }

    /**
     * 定位信息改变
     */
    public static class EventUpdateLocationInfo {
        public GeoPoint gcj2000Point;
        public double accuracy;

        public EventUpdateLocationInfo(GeoPoint gcj2000Point, double accuracy) {
            this.gcj2000Point = gcj2000Point;
            this.accuracy = accuracy;
        }
    }

    /**
     * 朝向信息改变
     */
    public static class EventUpdateDirection {
        public float direction;

        public EventUpdateDirection(float direction) {
            this.direction = direction;
        }
    }

    /**
     * 刷新shp列表
     */
    public static class EventUpdateShpList {


    }

    /**
     * 显示下载布局
     */
    public static class EventShowSelectTilesLayout {

        public ITileSource tileSource;

        public EventShowSelectTilesLayout(ITileSource tileSource) {

            this.tileSource = tileSource;
        }
    }

    /**
     * 取消编辑图形
     */
    public static class EventCancelEditGeometry {


    }

    /**
     * 可以编辑图形
     */
    public static class EventEditGeometry {
        public ISelectOverlay identifyShp;

        public EventEditGeometry(ISelectOverlay identifyShp) {
            this.identifyShp = identifyShp;
        }
    }

}
