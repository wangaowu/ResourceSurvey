package org.osmdroid.customImpl.geopackage.load;

import android.content.Context;

import org.osmdroid.overlay.bean.MultiOverlayWrapper;
import org.osmdroid.overlay.converter.OverlayClickListenerDispatcher;
import org.osmdroid.overlay.render.OsmdroidMapRender;
import org.osmdroid.views.MapView;

import java.io.File;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：Load Geopackage 的实现
 *
 * @author gwwang
 * @date 2021/12/16 9:51
 */
public class LoadGeopackageImpl extends BaseGeopackageLoad {
    private static final String TAG = "LoadGeopackageImpl";

    /**
     * 构造方法
     *
     * @param context  上下文
     * @param gpkgFile gpkg文件
     */
    public LoadGeopackageImpl(Context context, File gpkgFile) {
        super(context, gpkgFile);
    }

    /**
     * 执行加载 (please使用异步)
     *
     * @param featureTable        feature表名
     * @param TableResultListener 每行结果回调
     */
    public void syncLoadAll(String featureTable, TableResultListener TableResultListener) {
        syncLoad(featureTable, null, null, TableResultListener);
    }

    /**
     * 使用指定render添加到mapview
     *
     * @param mapView
     * @param render
     * @param featureRow
     * @param markFieldName
     * @param clickShpListener
     * @return
     */
    public MultiOverlayWrapper add2MapView(MapView mapView, OsmdroidMapRender render, FeatureRow featureRow, String markFieldName, Object clickShpListener) {
        MultiOverlayWrapper overlayWrapper = render.addOverlayUseFeature(mapView, featureRow, markFieldName);
        new OverlayClickListenerDispatcher(overlayWrapper).dispatch(clickShpListener);
        return overlayWrapper;
    }
}
