package com.bytemiracle.resourcesurvey.modules.main;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.basecompunent.TViewProxy;
import com.bytemiracle.resourcesurvey.common.compunent.ListenableController;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.VectorLayerManageFragment;
import com.bytemiracle.resourcesurvey.modules.main.popfragment.EditableLayersFragment;
import com.bytemiracle.resourcesurvey.modules.trajectory.TrajectoryBizz;
import com.bytemiracle.resourcesurvey.modules.trajectory.TrajectoryFragmentDialog;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.greenrobot.eventbus.EventBus;
import org.osmdroid.edit.bean.MeasureMode;
import org.osmdroid.measure.OsmMeasurePresenter2;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapBaseUtils;
import org.osmdroid.overlay.utils.MapOverlayUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/5/22 16:03
 */
public class HoverToolsController implements ListenableController {
    @BindView(R.id.view_editable_layers)
    View viewEditableLayers;
    @BindView(R.id.view_layers)
    View viewLayers;
    @BindView(R.id.view_identify)
    TViewProxy viewIdentify;
    @BindView(R.id.view_trajectory)
    TViewProxy viewTrajectory;
    @BindView(R.id.view_measure_distance)
    TViewProxy viewMeasureDistance;
    @BindView(R.id.view_measure_area)
    TViewProxy viewMeasureArea;
    @BindView(R.id.view_my_location)
    View viewMyLocation;
    @BindView(R.id.view_full_screen)
    View viewFullScreen;
    @BindView(R.id.layout_geometry_tools)
    View layoutGeometryTools;

    private Fragment fragment;
    private TrajectoryBizz trajectoryBizz;

    private ListenableController geometryEditToolsController;

    public HoverToolsController(Fragment fragment, View layoutProjectFunction, TrajectoryBizz trajectoryBizz) {
        this.fragment = fragment;
        this.trajectoryBizz = trajectoryBizz;
        ButterKnife.bind(this, layoutProjectFunction);
        this.geometryEditToolsController = new GeometryEditToolsController(layoutGeometryTools);
        initListener();
    }

    private void initListener() {
        //图层管理
        viewLayers.setOnClickListener(v -> {
            if (GlobalObjectHolder.getOpeningProject() == null) {
                XToastUtils.info("请先创建工程!");
                return;
            }
            new VectorLayerManageFragment().show(fragment.getChildFragmentManager(), "");
        });
        //可编辑图层
        viewEditableLayers.setOnClickListener(v -> new EditableLayersFragment().show(fragment.getChildFragmentManager(), ""));
        //选择
        viewIdentify.setOnClickListener(v -> {
            List<PackageOverlay> visiblePackageOverlays = MapOverlayUtils.getVisibleMapPackageOverlays(MapElementsHolder.getMapView());
            if (viewIdentify.getButtonState() == TViewProxy.STATE.CLICKABLE) {
                viewIdentify.setButtonState(TViewProxy.STATE.ENABLE);
                //选择
                MapOverlayUtils.setOverlaySelectState(visiblePackageOverlays, true);
            } else {
                viewIdentify.setButtonState(TViewProxy.STATE.CLICKABLE);
                //取消选择，让图层不可选中
                MapElementsHolder.setIdentifyShape(null);
                MapOverlayUtils.clearHighlightFeature(MapElementsHolder.getMapView());
                MapOverlayUtils.setOverlaySelectState(visiblePackageOverlays, false);
                EventBus.getDefault().post(new EventCluster.EventCancelEditGeometry());
                EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
            }
        });
        //轨迹管理
        trajectoryBizz.bindTrajectoryButton(viewTrajectory);
        viewTrajectory.setOnClickListener(v -> {
            if (GlobalObjectHolder.getOpeningProject() == null) {
                XToastUtils.info("请先创建工程!");
                return;
            }
            new TrajectoryFragmentDialog(trajectoryBizz).show(fragment.getChildFragmentManager(), "");
        });
        //全图按钮
        viewFullScreen.setOnClickListener(v -> {
            MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
        });

        OsmMeasurePresenter2 cus_measure = new OsmMeasurePresenter2(MapElementsHolder.getMapView(), "cus_measure");

        //测量
        viewMeasureDistance.setOnClickListener(v -> {
            if (viewMeasureDistance.getButtonState() == TViewProxy.STATE.CLICKABLE) {
                viewMeasureArea.setButtonState(TViewProxy.STATE.CLICKABLE);
                viewMeasureDistance.setButtonState(TViewProxy.STATE.ENABLE);
                cus_measure.changeMeasureMode(MeasureMode.DISTANCE);
            } else if (viewMeasureDistance.getButtonState() == TViewProxy.STATE.ENABLE) {
                cus_measure.clearGraphicInfo();
                viewMeasureDistance.setButtonState(TViewProxy.STATE.CLICKABLE);
            }
        });
        viewMeasureArea.setOnClickListener(v -> {
            if (viewMeasureArea.getButtonState() == TViewProxy.STATE.CLICKABLE) {
                viewMeasureDistance.setButtonState(TViewProxy.STATE.CLICKABLE);
                viewMeasureArea.setButtonState(TViewProxy.STATE.ENABLE);
                cus_measure.changeMeasureMode(MeasureMode.AREA);
            } else if (viewMeasureArea.getButtonState() == TViewProxy.STATE.ENABLE) {
                cus_measure.clearGraphicInfo();
                viewMeasureArea.setButtonState(TViewProxy.STATE.CLICKABLE);
            }
        });
    }

    /**
     * 获取定位按钮
     *
     * @return
     */
    public View getLocateView() {
        return viewMyLocation;
    }

    @Override
    public void onDestroyView() {
        geometryEditToolsController.onDestroyView();
    }
}
