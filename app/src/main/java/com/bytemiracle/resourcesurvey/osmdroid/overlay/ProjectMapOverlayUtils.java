package com.bytemiracle.resourcesurvey.osmdroid.overlay;

import android.text.TextUtils;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyle;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.xuexiang.xui.widget.dialog.LoadingDialog;

import org.osmdroid.customImpl.geopackage.load.FeatureTableResultDTO;
import org.osmdroid.customImpl.geopackage.load.LoadGeopackageImpl;
import org.osmdroid.customImpl.geopackage.load.TableResultListener;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.bean.options.OsmRenderOption;
import org.osmdroid.overlay.render.IWMarker;
import org.osmdroid.overlay.render.IWPolygon;
import org.osmdroid.overlay.render.IWPolyline;
import org.osmdroid.overlay.render.OsmdroidMapRender;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapBaseUtils;
import org.osmdroid.overlay.utils.MapOverlayUtils;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.util.List;
import java.util.Map;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/3/4 10:39
 */
public class ProjectMapOverlayUtils extends MapOverlayUtils {

    /**
     * 重新加载所有的overlay
     *
     * @param loadingDialog
     * @param loadCompleteListener
     */
    public static void reloadAllFeatures(LoadingDialog loadingDialog, CommonAsyncListener<BoundingBox> loadCompleteListener) {
        String projectName = GlobalObjectHolder.getOpeningProject().getName();
        if (TextUtils.isEmpty(projectName)) {
            MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
            loadCompleteListener.doSomething(null);
            return;
        }
        //移除已有的图层
        MapView mapView = MapElementsHolder.getMapView();
        List<PackageOverlay> mapPackageOverlays = getMapGPKGFoldOverlays(mapView);
        for (PackageOverlay mapPackageOverlay : mapPackageOverlays) {
            mapPackageOverlay.getItems().clear();
        }
        //重新解析gpkg文件
        loadingDialog.updateMessage("获取要素中...");
        String gpkgPath = ProjectUtils.getProjectGeoPackage(projectName);
        LoadGeopackageImpl loadGeopackage = new LoadGeopackageImpl(mapView.getContext(), new File(gpkgPath));
        GlobalInstanceHolder.newSingleExecutor().execute(() ->
                loadGeopackage.syncLoadAll(new TableResultListener() {
                    @Override
                    public void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult) {
                        if (tableResult == null) {
                            GlobalInstanceHolder.mainHandler().post(() -> loadCompleteListener.doSomething(null));
                        } else {
                            GlobalInstanceHolder.mainHandler().post(() -> loadingDialog.updateMessage("绘制要素中..."));
                            BoundingBox boundingBox = null;
                            for (Map.Entry<String, FeatureTableResultDTO> overlayEntry : tableResult.entrySet()) {
                                String overlayName = overlayEntry.getKey();
                                PackageOverlayInfo.OSMGeometryType osmGeometryType = overlayEntry.getValue().getGeometryType();
                                List<FeatureRow> features = overlayEntry.getValue().getFeatureRows();
                                //渲染配置
                                OverlayRenderStyle overlayStyle = OverlayRenderStyleUtils.getOverlayStyle(overlayName);
                                OsmRenderOption renderOption = OverlayRenderStyleUtils.getConfigRenderOption(overlayStyle);
                                OsmdroidMapRender render = new OsmdroidMapRender(mapView, renderOption, overlayName, gpkgPath, osmGeometryType);
                                render.removeItems();
                                if (!ListUtils.isEmpty(features)) {
                                    for (FeatureRow feature : features) {
                                        Object clickOverlayListener = MapElementsHolder.clickOverlayListeners.get(osmGeometryType);
                                        loadGeopackage.add2MapView(mapView, render, feature, overlayStyle.getMarkFieldName(), clickOverlayListener);
                                    }
                                }
                                BoundingBox layerBox = render.getPackageOverlay().getBounds();
                                boundingBox = boundingBox == null ? layerBox : boundingBox.concat(layerBox);
                            }
                            BoundingBox finalBoundingBox = boundingBox;
                            GlobalInstanceHolder.mainHandler().post(() -> loadCompleteListener.doSomething(finalBoundingBox));
                        }
                    }
                }));
    }

    /**
     * 重新加载该图层的所有要素
     *
     * @param overlay
     */
    public static void reloadOverlayFeatures(PackageOverlay overlay) {
        overlay.getItems().clear();
        String tableName = overlay.getName();
        String gpkgPath = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
        MapView mapView = MapElementsHolder.getMapView();
        LoadGeopackageImpl loadGeopackage = new LoadGeopackageImpl(GlobalInstanceHolder.applicationContext(), new File(gpkgPath));
        loadGeopackage.syncLoadAll(new TableResultListener() {
            @Override
            public void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult) {
                if (tableResult != null && tableResult.containsKey(tableName)) {
                    PackageOverlayInfo.OSMGeometryType osmGeometryType = tableResult.get(tableName).getGeometryType();
                    List<FeatureRow> featureRows = tableResult.get(tableName).getFeatureRows();
                    if (!ListUtils.isEmpty(featureRows)) {
                        //渲染配置
                        OverlayRenderStyle overlayStyle = OverlayRenderStyleUtils.getOverlayStyle(tableName);
                        OsmRenderOption renderOption = OverlayRenderStyleUtils.getConfigRenderOption(overlayStyle);
                        OsmdroidMapRender render = new OsmdroidMapRender(mapView, renderOption, overlay);
                        for (FeatureRow feature : featureRows) {
                            Object clickOverlayListener = MapElementsHolder.clickOverlayListeners.get(osmGeometryType);
                            loadGeopackage.add2MapView(mapView, render, feature, overlayStyle.getMarkFieldName(), clickOverlayListener);
                        }
                    }
                }
            }
        });
    }

    /**
     * 重新加载该图层的所有要素
     *
     * @param overlayName
     */
    public static void addNewEmptyOverlay(String overlayName) {
        String gpkgPath = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
        LoadGeopackageImpl loadGeopackage = new LoadGeopackageImpl(GlobalInstanceHolder.applicationContext(), new File(gpkgPath));
        loadGeopackage.syncLoadAll(new TableResultListener() {
            @Override
            public void onReadTableResult(Map<String, FeatureTableResultDTO> tableResult) {
                if (tableResult != null && tableResult.containsKey(overlayName)) {
                    //图层类型
                    PackageOverlayInfo.OSMGeometryType osmGeometryType = tableResult.get(overlayName).component1();
                    //渲染配置
                    OsmRenderOption renderOption = OverlayRenderStyleUtils.getConfigRenderOption(overlayName);
                    new OsmdroidMapRender(MapElementsHolder.getMapView(), renderOption, overlayName, gpkgPath, PackageOverlayInfo.Category.GEOPACKAGE, osmGeometryType);
                }
            }
        });
    }


    /**
     * 匹配图形类型的icon指示
     *
     * @param overlay
     * @return
     */
    public static int matchTypeIcon(PackageOverlay overlay) {
        List<Overlay> featureGeometries = overlay.getItems();
        if (ListUtils.isEmpty(featureGeometries)) {
            return 0;
        }
        Overlay featureGeometry = featureGeometries.get(0);
        if (featureGeometry instanceof IWMarker) {
            return R.drawable.ic_layer_type_dot;
        } else if (featureGeometry instanceof IWPolyline) {
            return R.drawable.ic_layer_type_polyline;
        } else if (featureGeometry instanceof IWPolygon) {
            return R.drawable.ic_layer_type_polygon;
        } else {
            return 0;
        }
    }
}
