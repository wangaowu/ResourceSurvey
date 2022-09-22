package com.bytemiracle.resourcesurvey.modules.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.fragment.CoreFragmentManager;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.view.BaseCheckPojo;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.ButtonProxy;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.bytemiracle.resourcesurvey.common.view.AppTabLayout;
import com.bytemiracle.resourcesurvey.modules.settings.SettingsFragment;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xutil.XUtil;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.cache.CacheTilesComponent;
import org.osmdroid.customImpl.convert.Shp2GeoPackageImpl;
import org.osmdroid.customImpl.convert.transform.To84GeometryWktImpl;
import org.osmdroid.overlay.utils.MapBaseUtils;
import org.osmdroid.overlay.utils.MapOverlayUtils;
import org.osmdroid.views.MapView;

import java.util.List;

import butterknife.BindView;
import me.rosuh.filepicker.config.FilePickerManager;

/**
 * 类功能：业务首页
 *
 * @author gwwang
 * @date 2021/5/21 9:03
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    public static final int CODE_REQ_SHP_FILE = 1;

    public LoadingDialog mLoadingDialog;

    @BindView(R.id.tabs_top_function)
    public AppTabLayout tabsTopFunction;

    @BindView(R.id.iv_settings)
    ImageView ivSettings;
    @BindView(R.id.ll_tools)
    LinearLayout llTools;

    //顶部栏数据
    private final List<AppTabLayout.Pojo> TOP_TABS = MainTabsProvider.getTopTabs();

    private CoreFragmentManager coreFragmentManager;
    private ButtonProxy btSettings;
    public GeometryCreateToolsController geometryCreateToolsController;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initViewsWithSavedInstanceState(Bundle savedInstanceState) {
        GlobalObjectHolder.setMainActivityObject(this);
        ProjectUtils.ensureSampleGeoPackage();

        mLoadingDialog = WidgetUtils.getLoadingDialog(this).setIconScale(0.4F).setLoadingSpeed(8);
        mLoadingDialog.setCancelable(true);
        mLoadingDialog.updateMessage("加载中……");

        coreFragmentManager = CoreFragmentManager.newInstance(this, R.id.fl_container);
        geometryCreateToolsController = new GeometryCreateToolsController(llTools);
        //设置按钮点击事件
        btSettings = new ButtonProxy(ivSettings, R.drawable.ic_settings, R.drawable.ic_focus_settings, R.drawable.ic_settings);
        btSettings.setOnClickListener(v -> {
            if (btSettings.getButtonState() == ButtonProxy.STATE.CLICKABLE) {
                //清空顶部栏选中
                BaseCheckPojo.clearCheckedItem(TOP_TABS);
                initTopTabsListener();
                //切换到设置页面
                btSettings.setButtonState(ButtonProxy.STATE.ENABLE);
                coreFragmentManager.switch2Fragment(SettingsFragment.class);
            }
        });

        //设置最近打开的工程信息
        setupOpeningProject();

        //初始化顶部栏
        showMainSubTabs(null);
        initTopTabsListener();
        coreFragmentManager.switch2Fragment(MainFragment.class);
    }

    private void setupOpeningProject() {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        DBProject latestProject = dbProjectDao.queryBuilder().where(DBProjectDao.Properties.IsLatestProject.eq(1)).unique();
        GlobalObjectHolder.setOpeningProject(latestProject);
    }

    private void showMainSubTabs(List<AppTabLayout.Pojo> subTabs) {
        MainFragment mainFragment = coreFragmentManager.findFragment(MainFragment.class);
        if (mainFragment == null) {
            return;
        }
        if (ListUtils.isEmpty(subTabs)) {
            mainFragment.tabsTopSubFunction.setVisibility(View.GONE);
            return;
        }
        mainFragment.tabsTopSubFunction.setVisibility(View.VISIBLE);
        mainFragment.tabsTopSubFunction.initTabs(subTabs, (pojo, isClick) -> {
            if (isClick) {
                MainTabsProvider.dispatchSubClickListener(MainActivity.this, pojo,
                        needCheckCurrentTab -> {
                            BaseCheckPojo.clearCheckedItem(subTabs);
                            if (needCheckCurrentTab) {
                                pojo.setChecked(true);
                            }
                            showMainSubTabs(subTabs);
                        });
            }
        });
    }

    private void initTopTabsListener() {
        tabsTopFunction.initTabs(TOP_TABS, (pojo, isClick) -> {
            if ("数据录入".equals(pojo.text)) {
                //无论选中or取消选中第一tab，均确认首页底图
                coreFragmentManager.switch2Fragment(pojo.fragmentClazz);
                boolean checked = pojo.isChecked();
                if (isClick) {
                    if (checked) {
                        BaseCheckPojo.checkedSingleItem(TOP_TABS, 0);
                        initTopTabsListener();
                        showMainSubTabs(MainTabsProvider.getTopSubTabs("数据录入"));
                    } else {
                        showMainSubTabs(null);
                    }
                }
            } else {
                if (isClick) {
                    //使用数据驱动单选操作
                    BaseCheckPojo.clearCheckedItem(TOP_TABS);
                    pojo.setChecked(true);
                    initTopTabsListener();
                    //设置按钮
                    btSettings.setButtonState(ButtonProxy.STATE.CLICKABLE);
                }
                if (pojo.isChecked()) {
                    //显示绑定的fragment
                    coreFragmentManager.switch2Fragment(pojo.fragmentClazz);
                }
            }
        });
    }

    /**
     * 切换到首页（地图）Fragment
     */
    public void switch2MainFragment() {
        BaseCheckPojo.clearCheckedItem(TOP_TABS);
        tabsTopFunction.checkFirst();
    }

    @Override
    protected int getStatusBarColor() {
        return getColor(R.color.app_common_bg_cyanotic);
    }

    @Override
    protected boolean allowBackHome() {
        //优先关闭绘制图形选项
        MainFragment mainFragment = coreFragmentManager.findFragment(MainFragment.class);
        List<AppTabLayout.Pojo> subTabs = mainFragment.tabsTopSubFunction.getTabDatas();
        if (!ListUtils.isEmpty(subTabs)) {
            BaseCheckPojo subCheckedItem = BaseCheckPojo.getSingleCheckedItem(subTabs);
            if (subCheckedItem instanceof AppTabLayout.Pojo) {
                if ("绘制图形".equals(((AppTabLayout.Pojo) subCheckedItem).text)) {
                    subCheckedItem.setChecked(false);
                    geometryCreateToolsController.closeTools();
                    showMainSubTabs(subTabs);
                    return false;
                }
            }
        }
        //优先清空高亮feature的相关操作
        if (MapElementsHolder.getIdentifyShape() != null) {
            MapElementsHolder.setIdentifyShape(null);
            MapOverlayUtils.clearHighlightFeature(MapElementsHolder.getMapView());
            EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
            EventBus.getDefault().post(new EventCluster.EventCancelEditGeometry());
            return false;
        }
        //确认关闭瓦片缓存服务
        if (CacheTilesComponent.serviceIsRunning) {
            XToastUtils.info("瓦片缓存未完成，请稍后!");
            return false;
        }
        new MaterialDialog.Builder(this)
                .content("您要退出系统吗？")
                .positiveText("确定")
                .onPositive((dialog, which) -> XUtil.get().exitApp())
                .negativeText("取消")
                .show();
        return false;
    }

    @Override
    protected void onPause() {
        MapView mapViewObject = MapElementsHolder.getMapView();
        if (mapViewObject != null) {
            mapViewObject.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MapView mapViewObject = MapElementsHolder.getMapView();
        if (mapViewObject != null) {
            mapViewObject.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        MapView mapViewObject = MapElementsHolder.getMapView();
        if (mapViewObject != null) {
            mapViewObject.onDetach();
        }
        geometryCreateToolsController.onDestroyView();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQ_SHP_FILE && resultCode == Activity.RESULT_OK) {
            String projectGeoPackage = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
            String shpPath = FilePickerManager.INSTANCE.obtainData().get(0);
            mLoadingDialog.updateMessage("正在导入..");
            mLoadingDialog.show();
            //1.导入shp文件到geopackage
            new Shp2GeoPackageImpl(shpPath, projectGeoPackage, new To84GeometryWktImpl()).execute(geoFeatureTableName -> {
                mLoadingDialog.dismiss();
                if (!TextUtils.isEmpty(geoFeatureTableName)) {
                    //2.插入图层渲染配置
                    OverlayRenderStyleUtils.insertOverlayConfig(geoFeatureTableName);
                    //3. 重新加载所有图层
                    ProjectMapOverlayUtils.reloadAllFeatures(mLoadingDialog, boundingBox -> {
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                        XToastUtils.info("导入成功!");
                    });
                }
            });
        }
    }
}
