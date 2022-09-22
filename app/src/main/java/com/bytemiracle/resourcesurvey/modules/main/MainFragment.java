package com.bytemiracle.resourcesurvey.modules.main;

import android.view.View;
import android.view.ViewGroup;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.fragment.BaseFragment;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.AsyncCombine;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.sensor.DirectionManager;
import com.bytemiracle.resourcesurvey.common.view.AppSubTabLayout;
import com.bytemiracle.resourcesurvey.giscommon.Raster_;
import com.bytemiracle.resourcesurvey.giscommon.location.AppLocationManager;
import com.bytemiracle.resourcesurvey.giscommon.location.LocationDataChangedListener;
import com.bytemiracle.resourcesurvey.modules.trajectory.TrajectoryBizz;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.overlay.OnlineMapHolder;
import org.osmdroid.overlay.utils.MapBaseUtils;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.overlay.utils.MapOverlayUtils;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.List;

import butterknife.BindView;

/**
 * 类功能：地图页面
 *
 * @author gwwang
 * @date 2021/5/21 11:22
 */
@FragmentTag(name = "主界面")
public class MainFragment extends BaseFragment {
    private static final String TAG = "MainFragment";

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.layout_hover_function)
    View layoutProjectFunction;
    @BindView(R.id.tabs_top_sub_function)
    public AppSubTabLayout tabsTopSubFunction;

    private DirectionManager directionManager;
    private AppLocationManager locationManager;

    private GeoPoint myLocation = null;

    private HoverToolsController hoverToolsController;

    //工程切换的事件
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent1(EventCluster.EventChangeProject event) {
        MapOverlayUtils.clearMapOverlaysWithIW(mapView);
        MapOverlayUtils.clearMapBaseOverlays(mapView);
        MapElementsHolder.getEditableOverlays().clear();
        MapElementsHolder.setCurrentEditOverlay(null);
        mLoadingDialog.updateMessage("加载工程中...");
        mLoadingDialog.show();
        loadTileOverlays(completed ->
                loadProjectLayers(o1 -> {
                    EventBus.getDefault().post(new EventCluster.EventAllLayersLoaded());
                    mLoadingDialog.dismiss();
                }));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initViews() {
        EventBus.getDefault().register(this);
        MapElementsHolder.setMapView(mapView);
        //轨迹业务类
        TrajectoryBizz trajectoryBizz = new TrajectoryBizz((BaseActivity) getActivity(), mapView);
        hoverToolsController = new HoverToolsController(this, layoutProjectFunction, trajectoryBizz);
        hoverToolsController.getLocateView().setOnClickListener(v -> {
            if (myLocation != null) {
                mapView.getController().animateTo(myLocation, MapConstant.LOCATION_ZOOM_LEVEL, 500L);
                return;
            }
            XToastUtils.info("正在定位...");
            startLocation();
        });

        //位置管理器
        locationManager = AppLocationManager.getInstance(getContext());
        //方向传感器
        directionManager = DirectionManager.getInstance(getContext());
        directionManager.registerDirectionListener(direction -> {
            EventBus.getDefault().post(new EventCluster.EventUpdateDirection(360 - direction));
        });

        initOsmdroidMap();
    }

    private void initOsmdroidMap() {
        mLoadingDialog.updateMessage("加载工程中...");
        mLoadingDialog.show();
        loadTileOverlays(completed ->
                loadProjectLayers(o1 -> {
                    EventBus.getDefault().post(new EventCluster.EventAllLayersLoaded());
                    mLoadingDialog.dismiss();
                    startLocation();
                }));
    }

    private void startLocation() {
        locationManager.startLocate(getActivity(), 10, new LocationDataChangedListener() {
            @Override
            public void onProceedChanged(GeoPoint oldPoint, GeoPoint newPoint, double accuracy, double headDirection) {
                EventBus.getDefault().post(new EventCluster.EventUpdateLocationInfo(newPoint, accuracy));
                if ((myLocation = newPoint) != null) {
                    mapView.setExpectedCenter(newPoint);
                    locationManager.stopLocate();
                }
            }
        });
    }

    private void loadTileOverlays(CommonAsyncListener<Object> loadCompleteListener) {
        MapBaseUtils.initConfig(mapView);
        OnlineMapHolder.Companion.init(getContext());
        MapOverlayUtils.loadTileLayers(mapView, OnlineMapHolder.Companion.getTileOverlays());
        loadCompleteListener.doSomething(true);
    }

    private void loadProjectLayers(CommonAsyncListener loadCompleteListener) {
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        if (openingProject == null) {
            loadCompleteListener.doSomething(null);
            return;
        }
        //加载矢量图层
        ProjectMapOverlayUtils.reloadAllFeatures(mLoadingDialog, boundingBox -> {
                    MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                    //加载栅格图层
                    loadRasterLayers(loadCompleteListener);
                }
        );
    }

    /**
     * 加载栅格图层
     */
    private void loadRasterLayers(CommonAsyncListener<Object> loadCompleteListener) {
        List<String> rasterPaths = Raster_.getRasterPaths();
        if (ListUtils.isEmpty(rasterPaths)) {
            loadCompleteListener.doSomething(null);
            return;
        }
        AsyncCombine asyncCombine = new AsyncCombine(rasterPaths.size(), loadCompleteListener);
        for (String rasterPath : rasterPaths) {
            Raster_.addRasterLayer2Map(rasterPath, o -> asyncCombine.completeSelf());
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        directionManager.unRegisterDirectionListener();
        hoverToolsController.onDestroyView();
        super.onDestroy();
    }

}
