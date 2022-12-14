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
 * ????????????????????????
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

    //???????????????
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
        mLoadingDialog.updateMessage("???????????????");

        coreFragmentManager = CoreFragmentManager.newInstance(this, R.id.fl_container);
        geometryCreateToolsController = new GeometryCreateToolsController(llTools);
        //????????????????????????
        btSettings = new ButtonProxy(ivSettings, R.drawable.ic_settings, R.drawable.ic_focus_settings, R.drawable.ic_settings);
        btSettings.setOnClickListener(v -> {
            if (btSettings.getButtonState() == ButtonProxy.STATE.CLICKABLE) {
                //?????????????????????
                BaseCheckPojo.clearCheckedItem(TOP_TABS);
                initTopTabsListener();
                //?????????????????????
                btSettings.setButtonState(ButtonProxy.STATE.ENABLE);
                coreFragmentManager.switch2Fragment(SettingsFragment.class);
            }
        });

        //?????????????????????????????????
        setupOpeningProject();

        //??????????????????
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
            if ("????????????".equals(pojo.text)) {
                //????????????or??????????????????tab????????????????????????
                coreFragmentManager.switch2Fragment(pojo.fragmentClazz);
                boolean checked = pojo.isChecked();
                if (isClick) {
                    if (checked) {
                        BaseCheckPojo.checkedSingleItem(TOP_TABS, 0);
                        initTopTabsListener();
                        showMainSubTabs(MainTabsProvider.getTopSubTabs("????????????"));
                    } else {
                        showMainSubTabs(null);
                    }
                }
            } else {
                if (isClick) {
                    //??????????????????????????????
                    BaseCheckPojo.clearCheckedItem(TOP_TABS);
                    pojo.setChecked(true);
                    initTopTabsListener();
                    //????????????
                    btSettings.setButtonState(ButtonProxy.STATE.CLICKABLE);
                }
                if (pojo.isChecked()) {
                    //???????????????fragment
                    coreFragmentManager.switch2Fragment(pojo.fragmentClazz);
                }
            }
        });
    }

    /**
     * ???????????????????????????Fragment
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
        //??????????????????????????????
        MainFragment mainFragment = coreFragmentManager.findFragment(MainFragment.class);
        List<AppTabLayout.Pojo> subTabs = mainFragment.tabsTopSubFunction.getTabDatas();
        if (!ListUtils.isEmpty(subTabs)) {
            BaseCheckPojo subCheckedItem = BaseCheckPojo.getSingleCheckedItem(subTabs);
            if (subCheckedItem instanceof AppTabLayout.Pojo) {
                if ("????????????".equals(((AppTabLayout.Pojo) subCheckedItem).text)) {
                    subCheckedItem.setChecked(false);
                    geometryCreateToolsController.closeTools();
                    showMainSubTabs(subTabs);
                    return false;
                }
            }
        }
        //??????????????????feature???????????????
        if (MapElementsHolder.getIdentifyShape() != null) {
            MapElementsHolder.setIdentifyShape(null);
            MapOverlayUtils.clearHighlightFeature(MapElementsHolder.getMapView());
            EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
            EventBus.getDefault().post(new EventCluster.EventCancelEditGeometry());
            return false;
        }
        //??????????????????????????????
        if (CacheTilesComponent.serviceIsRunning) {
            XToastUtils.info("?????????????????????????????????!");
            return false;
        }
        new MaterialDialog.Builder(this)
                .content("????????????????????????")
                .positiveText("??????")
                .onPositive((dialog, which) -> XUtil.get().exitApp())
                .negativeText("??????")
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
            mLoadingDialog.updateMessage("????????????..");
            mLoadingDialog.show();
            //1.??????shp?????????geopackage
            new Shp2GeoPackageImpl(shpPath, projectGeoPackage, new To84GeometryWktImpl()).execute(geoFeatureTableName -> {
                mLoadingDialog.dismiss();
                if (!TextUtils.isEmpty(geoFeatureTableName)) {
                    //2.????????????????????????
                    OverlayRenderStyleUtils.insertOverlayConfig(geoFeatureTableName);
                    //3. ????????????????????????
                    ProjectMapOverlayUtils.reloadAllFeatures(mLoadingDialog, boundingBox -> {
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                        XToastUtils.info("????????????!");
                    });
                }
            });
        }
    }
}
