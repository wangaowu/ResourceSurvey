package com.bytemiracle.resourcesurvey.modules.main;

import android.util.Log;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.preview.PreviewUtils;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.FeaturePropertyActivity;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.view.AppTabLayout;
import com.bytemiracle.resourcesurvey.modules.datamanage.DataManageFragment;
import com.bytemiracle.resourcesurvey.modules.help.HelpDocFragment;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.CreateFeatureTableFragment;
import com.bytemiracle.resourcesurvey.modules.main.popfragment.ManageProjectFragment;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;

import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapBaseUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import me.rosuh.filepicker.config.FilePickerManager;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.geopackage.geom.GeoPackageGeometryData;

public class MainTabsProvider {

    private static Map<String, List<String>> RELATION_CONTENT_DICT = new HashMap<>();

    static {
        RELATION_CONTENT_DICT.put("数据录入", Arrays.asList(new String[]{"工程管理", "新建图层", "导入图层", "绘制图形", "数据填报"}));
        RELATION_CONTENT_DICT.put("数据管理", Arrays.asList(new String[]{}));
        RELATION_CONTENT_DICT.put("数据校验", Arrays.asList(new String[]{}));
        RELATION_CONTENT_DICT.put("用户管理", Arrays.asList(new String[]{}));
        RELATION_CONTENT_DICT.put("注册用户", Arrays.asList(new String[]{}));
        RELATION_CONTENT_DICT.put("重新登陆", Arrays.asList(new String[]{}));
        RELATION_CONTENT_DICT.put("帮助", Arrays.asList(new String[]{}));
    }

    /**
     * 顶部栏数据
     *
     * @return
     */
    public static List<AppTabLayout.Pojo> getTopTabs() {
        return Arrays.asList(new AppTabLayout.Pojo[]{
                new AppTabLayout.Pojo(0, "数据录入", MainFragment.class, false, false),
                new AppTabLayout.Pojo(0, "数据管理", DataManageFragment.class, false, false),
                //new AppTabLayout.Pojo(0, "数据校验", DataValidFragment.class, false, false),
                //new AppTabLayout.Pojo(0, "用户管理", UserManageFragment.class, false, false),
                new AppTabLayout.Pojo(0, "帮助", HelpDocFragment.class, false, false)});
    }

    /**
     * 顶部栏子数据
     *
     * @param topContent
     * @return
     */
    public static List<AppTabLayout.Pojo> getTopSubTabs(String topContent) {
        List<String> subTabContents = RELATION_CONTENT_DICT.get(topContent);
        return subTabContents.stream()
                .map(subContent ->
                        new AppTabLayout.Pojo(0, subContent, false, false))
                .collect(Collectors.toList());
    }

    /**
     * 分发点击事件
     *
     * @param mainActivity             首页索引
     * @param clickedTab               点击的button
     * @param needCheckCurrentListener 更新状态监听
     */
    public static void dispatchSubClickListener(MainActivity mainActivity, AppTabLayout.Pojo clickedTab, CommonAsyncListener<Boolean> needCheckCurrentListener) {
        switch (clickedTab.text) {
            case "工程管理":
                new ManageProjectFragment().show(mainActivity.getSupportFragmentManager(), "");
                needCheckCurrentListener.doSomething(false);
                break;
            case "新建图层":
                if (GlobalObjectHolder.getOpeningProject() == null) {
                    XToastUtils.info("请先创建工程!");
                    needCheckCurrentListener.doSomething(false);
                    return;
                }
                new CreateFeatureTableFragment(GlobalObjectHolder.getOpeningProject(), tableName -> {
                    if (tableName != null) {
                        //1.将geoPackage解析后添加到map图层
                        ProjectMapOverlayUtils.addNewEmptyOverlay(tableName);
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                    }
                }).show(mainActivity.getSupportFragmentManager(), "");
                needCheckCurrentListener.doSomething(false);
                break;
            case "导入图层":
                if (GlobalObjectHolder.getOpeningProject() == null) {
                    XToastUtils.info("请先创建工程!");
                    needCheckCurrentListener.doSomething(false);
                    return;
                }
                FilePickerManager.from(mainActivity)
                        .enableSingleChoice()
                        .forResult(MainActivity.CODE_REQ_SHP_FILE);
                needCheckCurrentListener.doSomething(false);
                break;
            case "绘制图形":
                if (GlobalObjectHolder.getOpeningProject() == null) {
                    XToastUtils.info("请先创建工程!");
                    needCheckCurrentListener.doSomething(false);
                    return;
                }
                PackageOverlay editOverlay = MapElementsHolder.getCurrentEditOverlay();
                if (editOverlay == null) {
                    //没有可以编辑的图层
                    XToastUtils.info("没有可以编辑的图层");
                    needCheckCurrentListener.doSomething(false);
                    return;
                }
                if (clickedTab.isChecked()) {
                    //开始绘图
                    String tableName = editOverlay.getName();
                    PackageOverlayInfo.OSMGeometryType osmGeometryType = editOverlay.getPackageOverlayInfo().getOsmGeometryType();
                    mainActivity.geometryCreateToolsController.openTools(osmGeometryType, newGeometry -> {
                        //绘制的结果
                        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.updateMessage("正在更新..");
                        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.show();
                        editOverlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
                            FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                            FeatureRow newRow = featureDao.newRow();
                            newRow.setGeometry(new GeoPackageGeometryData(newGeometry));
                            if (featureDao.insert(newRow) > 0) {
                                try {
                                    GeoPackageQuick.sink2Database(geoPackage);
                                    new EditGeoPackage_(geoPackage).updateExtent(tableName, newGeometry.getEnvelope());
                                    geoPackage.close();
                                } catch (Exception e) {
                                    //只是修改边界有问题，无需认为失败
                                    Log.e("EditGeoPackage", "updateExtent: " + e.getMessage());
                                } finally {
                                    //关闭绘图选项
                                    GlobalInstanceHolder.mainHandler().post(() -> {
                                        ProjectMapOverlayUtils.reloadOverlayFeatures(editOverlay);
                                        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                                        mainActivity.geometryCreateToolsController.closeTools();
                                        needCheckCurrentListener.doSomething(false);
                                    });
                                }
                            }
                        });
                    });
                    needCheckCurrentListener.doSomething(true);
                } else {
                    //取消绘图
                    mainActivity.geometryCreateToolsController.closeTools();
                    needCheckCurrentListener.doSomething(false);
                }
                break;
            case "数据填报":
                if (GlobalObjectHolder.getOpeningProject() == null) {
                    XToastUtils.info("请先创建工程!");
                    needCheckCurrentListener.doSomething(false);
                    return;
                }
                if (MapElementsHolder.getIdentifyShape() != null) {
                    PreviewUtils.openActivity(mainActivity, FeaturePropertyActivity.class);
                } else {
                    XToastUtils.info("您还没有选择Feature!");
                }
                needCheckCurrentListener.doSomething(false);
                break;
        }
    }

    //以前的底部栏点击事件代码
    //   switch (subTabs.indexOf(pojo)) {
    //                    //绘图
    //                    case 1:
    //                        if (pojo.isChecked()) {
    //                            //选择
    //                            PackageOverlay editOverlay = MapElementsHolder.getCurrentEditOverlay();
    //                            if (editOverlay == null) {
    //                                //没有可以编辑的图层
    //                                pojo.setChecked(false);
    //                                initBottomTabsListener();
    //                                XToastUtils.info("没有可以编辑的图层");
    //                            } else {
    //                                //开始绘图
    //                                String tableName = editOverlay.getName();
    //                                geometryCreateToolsController.openTools(drawGeoPoints -> {
    //                                    //绘制的结果
    //                                    GlobalObjectHolder.getMainActivityObject().mLoadingDialog.updateMessage("正在更新..");
    //                                    GlobalObjectHolder.getMainActivityObject().mLoadingDialog.show();
    //                                    editOverlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
    //                                        List<Point> rowPoints = drawGeoPoints.stream()
    //                                                .map(geoPoint -> new Point(geoPoint.getLongitude(), geoPoint.getLatitude()))
    //                                                .collect(Collectors.toList());
    //                                        //暂时polygon只支持单面，没有hole,后续可以添加
    //                                        Polygon polygon = new Polygon(new LineString(rowPoints));
    //                                        FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
    //                                        FeatureRow newRow = featureDao.newRow();
    //                                        newRow.setGeometry(new GeoPackageGeometryData(polygon));
    //                                        if (featureDao.insert(newRow) > 0) {
    //                                            try {
    //                                                GeoPackageQuick.sink2Database(geoPackage);
    //                                                new EditGeoPackage_(geoPackage).updateExtent(tableName, polygon.getEnvelope());
    //                                                geoPackage.close();
    //                                            } catch (Exception e) {
    //                                                //只是修改边界有问题，无需认为失败
    //                                                Log.e(TAG, "updateExtent: " + e.getMessage());
    //                                            } finally {
    //                                                //关闭绘图选项
    //                                                GlobalInstanceHolder.mainHandler().post(() -> {
    //                                                    ProjectMapOverlayUtils.reloadOverlayFeatures(editOverlay);
    //                                                    GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
    //                                                    geometryCreateToolsController.closeTools();
    //                                                    pojo.setChecked(false);
    //                                                    initBottomTabsListener();
    //                                                });
    //                                            }
    //                                        }
    //                                    });
    //                                });
    //                            }
    //                        } else {
    //                            //取消选择
    //                            geometryCreateToolsController.closeTools();
    //                        }
    //                        break;
    //                    //属性
    //                    case 2:
    //                        if (pojo.isChecked()) {
    //                            if (MapElementsHolder.getIdentifyShape() != null) {
    //                                PreviewUtils.openActivity(this, FeaturePropertyActivity.class);
    //                            } else {
    //                                XToastUtils.info("您还没有选择Feature!");
    //                            }
    //                            pojo.setChecked(false);
    //                            initBottomTabsListener();
    //                        }
    //                        break;
    //                }
}
