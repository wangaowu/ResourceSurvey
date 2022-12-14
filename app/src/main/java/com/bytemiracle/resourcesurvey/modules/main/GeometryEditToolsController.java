package com.bytemiracle.resourcesurvey.modules.main;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.basecompunent.ButtonProxy;
import com.bytemiracle.resourcesurvey.common.basecompunent.TViewProxy;
import com.bytemiracle.resourcesurvey.common.compunent.ListenableController;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.bytemiracle.resourcesurvey.common.viewutil.ContextUtils;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.edit.OsmEditLinestringImpl;
import org.osmdroid.edit.OsmEditPointImpl;
import org.osmdroid.edit.OsmEditPolygonImpl;
import org.osmdroid.edit.base.IEditFeature;
import org.osmdroid.edit.bean.GraphicMode;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.bean.options.OsmRenderOption;
import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.overlay.render.IWMarker;
import org.osmdroid.overlay.render.IWPolygon;
import org.osmdroid.overlay.render.IWPolyline;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.views.overlay.OverlayWithIW;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * ????????????????????????????????????
 *
 * @author gwwang
 * @date 2021/5/22 10:15
 */
public class GeometryEditToolsController implements ListenableController {
    private static final String TAG = "GeometryToolsController";


    @BindView(R.id.tv_geometry_edit)
    public TViewProxy tvGeometryEdit;
    @BindView(R.id.layout_tools_container)
    View layoutToolsContainer;

    @BindView(R.id.iv_tools_sc)
    ImageView ivToolsSC;
    @BindView(R.id.iv_tools_cx)
    ImageView ivToolsCX;
    @BindView(R.id.iv_tools_xfg)
    ImageView ivToolsXFG;
    @BindView(R.id.iv_tools_hb)
    ImageView ivToolsHB;
    @BindView(R.id.iv_tools_mfg)
    ImageView ivToolsMFG;
    @BindView(R.id.iv_tools_hf)
    ImageView ivToolsHF;
    @BindView(R.id.iv_tools_bc)
    ImageView ivToolsBC;
    @BindView(R.id.iv_tools_add_hole)
    ImageView ivToolsAddHole;
    @BindView(R.id.iv_tools_delete_hole)
    ImageView ivToolsDeleteHole;

    private final Unbinder bind;

    public ButtonProxy btToolsSC;
    public ButtonProxy btToolsXFG;
    public ButtonProxy btToolsHB;
    public ButtonProxy btToolsMFG;
    public ButtonProxy btToolsCX;
    public ButtonProxy btToolsHF;
    public ButtonProxy btToolsZK;
    public ButtonProxy btToolsSK;
    public ButtonProxy btToolsBC;

    //?????????????????????
    private ISelectOverlay identifyGeometry;
    private IEditFeature editGraphicImpl;

    //???????????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent1(EventCluster.EventCancelEditGeometry event) {
        closeTools();
    }

    //?????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent2(EventCluster.EventEditGeometry event) {
        showGeometryEditButton(event.identifyShp);
    }

    public GeometryEditToolsController(View layoutGeometryTools) {
        bind = ButterKnife.bind(this, layoutGeometryTools);
        EventBus.getDefault().register(this);

        buildStateDrawables();
        initListeners();

        EventBus.getDefault().post(new EventCluster.EventEditGeometry(null));
    }

    public void goneEditButton() {
        showToolsContainer(false);
        tvGeometryEdit.setVisibility(View.GONE);
    }

    /**
     * ????????????
     */
    public void closeTools() {
        btToolsZK.setButtonState(ButtonProxy.STATE.CLICKABLE);
        btToolsSK.setButtonState(ButtonProxy.STATE.CLICKABLE);
        showToolsContainer(false);
        Drawable drawable = tvGeometryEdit.getContext().getResources().getDrawable(R.drawable.ic_edit_geometry);
        tvGeometryEdit.setCompoundDrawables(null, drawable, null, null);
        tvGeometryEdit.fixTopDrawable();
    }

    private void showGeometryEditButton(ISelectOverlay identifyGeometry) {
        this.identifyGeometry = identifyGeometry;
        tvGeometryEdit.setVisibility(identifyGeometry == null ? View.INVISIBLE : View.VISIBLE);
        if (identifyGeometry == null) {
            closeTools();
            if (editGraphicImpl != null) {
                editGraphicImpl.restoreCopyFeature();
            }
            return;
        }
        PackageOverlay packageOverlay = identifyGeometry.getFeatureOverlayInfo().getPackageOverlay();
        PackageOverlayInfo.OSMGeometryType osmGeometryType = packageOverlay.getPackageOverlayInfo().getOsmGeometryType();
        OsmRenderOption renderOption = OverlayRenderStyleUtils.getConfigRenderOption(packageOverlay.getName());
        editGraphicImpl = getEditGraphicImpl(osmGeometryType, renderOption);
        updateButtonStateByGeometry(identifyGeometry);
    }

    private IEditFeature getEditGraphicImpl(PackageOverlayInfo.OSMGeometryType osmGeometryType, OsmRenderOption renderOption) {
        switch (osmGeometryType) {
            case POINT:
                return new OsmEditPointImpl(MapElementsHolder.getMapView(), renderOption, MapConstant.TAG_GEOMETRY);
            case LINESTRING:
                return new OsmEditLinestringImpl(MapElementsHolder.getMapView(), renderOption, MapConstant.TAG_GEOMETRY);
            case POLYGON:
            default:
                return new OsmEditPolygonImpl(MapElementsHolder.getMapView(), renderOption, MapConstant.TAG_GEOMETRY);
        }
    }

    //??????????????????????????????
    //?????????:???????????????????????????????????????????????????????????????????????????
    private void updateButtonStateByGeometry(ISelectOverlay identifyGeometry) {
        if (identifyGeometry instanceof IWMarker) {
            //???
            btToolsXFG.setButtonState(ButtonProxy.STATE.GONE);
            btToolsHB.setButtonState(ButtonProxy.STATE.GONE);
            btToolsMFG.setButtonState(ButtonProxy.STATE.GONE);
            btToolsZK.setButtonState(ButtonProxy.STATE.GONE);
            btToolsSK.setButtonState(ButtonProxy.STATE.GONE);
        } else if (identifyGeometry instanceof IWPolyline) {
            //???
            btToolsXFG.setButtonState(ButtonProxy.STATE.CLICKABLE);
            btToolsHB.setButtonState(ButtonProxy.STATE.GONE);
            btToolsMFG.setButtonState(ButtonProxy.STATE.GONE);
            btToolsZK.setButtonState(ButtonProxy.STATE.GONE);
            btToolsSK.setButtonState(ButtonProxy.STATE.GONE);
        } else if (identifyGeometry instanceof IWPolygon) {
            //???
            btToolsXFG.setButtonState(ButtonProxy.STATE.CLICKABLE);
            btToolsHB.setButtonState(ButtonProxy.STATE.CLICKABLE);
            btToolsMFG.setButtonState(ButtonProxy.STATE.CLICKABLE);
            //?????????????????????
            boolean hasHoles = ((IWPolygon) identifyGeometry).hasHoles();
            btToolsSK.setButtonState(hasHoles ? ButtonProxy.STATE.CLICKABLE : ButtonProxy.STATE.DISABLE);
            btToolsZK.setButtonState(ButtonProxy.STATE.CLICKABLE);
        }
    }

    private void showToolsContainer(boolean show) {
        tvGeometryEdit.setText(show ? "??????" : "??????");
        layoutToolsContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initListeners() {
        showToolsContainer(false);
        tvGeometryEdit.setVisibility(View.GONE);
        tvGeometryEdit.setOnClickListener(v -> {
            PackageOverlay parentOverlay = identifyGeometry.getFeatureOverlayInfo().getPackageOverlay();
            if (parentOverlay != MapElementsHolder.getCurrentEditOverlay()) {
                XToastUtils.info("????????????????????????");
                return;
            }
            //????????????????????? && ????????????
            if (("??????").equals(tvGeometryEdit.getText())) {
                if (identifyGeometry instanceof OverlayWithIW) {
                    showToolsContainer(true);
                    //?????????????????????????????????feature??????????????????????????????graphic??????????????????
                    editGraphicImpl.hideOriginAndCopyFeature((OverlayWithIW) identifyGeometry);
                } else {
                    XToastUtils.info("????????????????????????!");
                }
            } else if ("??????".equals(tvGeometryEdit.getText())) {
                showToolsContainer(false);
                //????????????geometry
                editGraphicImpl.restoreCopyFeature();
            }
        });
        btToolsXFG.setOnClickListener(view -> {
            if (editGraphicImpl instanceof OsmEditPolygonImpl) {
                OsmEditPolygonImpl impl = (OsmEditPolygonImpl) editGraphicImpl;
                if (ButtonProxy.STATE.CLICKABLE == btToolsXFG.getButtonState()) {
                    XToastUtils.info("???????????????????????????!", Toast.LENGTH_LONG);
                    impl.switchMode(GraphicMode.DIVIDE_POLYGON);
                    btToolsXFG.setButtonState(ButtonProxy.STATE.ENABLE);
                } else if (ButtonProxy.STATE.ENABLE == btToolsXFG.getButtonState()) {
                    impl.undoDividePolygons();
                    btToolsXFG.setButtonState(ButtonProxy.STATE.CLICKABLE);
                }
            }
        });
        btToolsHB.setOnClickListener(view -> {
            XToastUtils.info("????????????!");


        });
        btToolsSK.setOnClickListener(view -> {
            if (editGraphicImpl instanceof OsmEditPolygonImpl) {
                OsmEditPolygonImpl impl = (OsmEditPolygonImpl) editGraphicImpl;
                if (ButtonProxy.STATE.CLICKABLE == btToolsSK.getButtonState()) {
                    XToastUtils.info("???????????????????????????????????????", Toast.LENGTH_LONG);
                    impl.switchMode(GraphicMode.DELETE_HOLE);
                    btToolsSK.setButtonState(ButtonProxy.STATE.ENABLE);
                } else if (ButtonProxy.STATE.ENABLE == btToolsSK.getButtonState()) {
                    impl.undoDeletedHoles();
                    btToolsSK.setButtonState(ButtonProxy.STATE.CLICKABLE);
                }
            }
        });
        btToolsZK.setOnClickListener(view -> {
            if (editGraphicImpl instanceof OsmEditPolygonImpl) {
                OsmEditPolygonImpl impl = (OsmEditPolygonImpl) editGraphicImpl;
                if (ButtonProxy.STATE.CLICKABLE == btToolsZK.getButtonState()) {
                    XToastUtils.info("?????????????????????????????????", Toast.LENGTH_LONG);
                    impl.switchMode(GraphicMode.CREATE_UNION_HOLE);
                    btToolsZK.setButtonState(ButtonProxy.STATE.ENABLE);
                } else if (ButtonProxy.STATE.ENABLE == btToolsZK.getButtonState()) {
                    impl.undoCreatedHoles();
                    btToolsZK.setButtonState(ButtonProxy.STATE.CLICKABLE);
                }
            }
        });
        btToolsCX.setOnClickListener(view -> editGraphicImpl.undoCachedStep());
        btToolsHF.setOnClickListener(view -> editGraphicImpl.redoCachedStep());
        btToolsBC.setOnClickListener(view -> prepareSaveEditGeometryResult());
        btToolsSC.setOnClickListener(view ->
                new CommonConfirmDialog("??????", "??????????????????????????????", new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog dialog) {
                        deleteIdentifyGeometry(dialog);
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog dialog) {
                        dialog.dismiss();
                    }
                }).show(GlobalObjectHolder.getMainActivityObject().getSupportFragmentManager(), ""));
    }

    private void deleteIdentifyGeometry(CommonConfirmDialog dialog) {
        if (identifyGeometry != null) {
            //??????????????????-->?????????
            FeatureOverlayInfo overlayInfo = identifyGeometry.getFeatureOverlayInfo();
            PackageOverlay packageOverlay = overlayInfo.getPackageOverlay();
            String tableName = packageOverlay.getName();
            FeatureRow featureRow = overlayInfo.getFeatureRow();
            packageOverlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
                FeatureDao featureDao = geoPackage.getFeatureDao(tableName);
                if (featureDao.delete(featureRow) > 0) {
                    GeoPackageQuick.sink2Database(geoPackage);
                    geoPackage.close();
                    //3.??????????????????????????????
                    editGraphicImpl.clearExistGraphicInfo();
                    //4.????????????????????????????????????????????????
                    MapElementsHolder.setIdentifyShape(null);
                    goneEditButton();
                    XToastUtils.info("????????????!");
                    ProjectMapOverlayUtils.reloadOverlayFeatures(packageOverlay);
                } else {
                    XToastUtils.info("????????????!");
                }
                dialog.dismiss();
            });
        }
    }

    //?????????????????????geometry
    private void prepareSaveEditGeometryResult() {
        BaseActivity activity = (BaseActivity) ContextUtils.getActivity(layoutToolsContainer.getContext());
        FeatureOverlayInfo featureOverlayInfo = identifyGeometry.getFeatureOverlayInfo();
        editGraphicImpl.prepareSaveEditGeometryResult(
                activity,
                featureOverlayInfo,
                (CommonAsyncListener<Boolean>) saveSuccess -> {
                    if (saveSuccess) {
                        MapElementsHolder.setIdentifyShape(null);
                        goneEditButton();
                        XToastUtils.info("????????????!");
                        ProjectMapOverlayUtils.reloadOverlayFeatures(featureOverlayInfo.getPackageOverlay());
                    } else {
                        XToastUtils.info("????????????!");
                    }
                });
    }

    private void buildStateDrawables() {
        btToolsSC = new ButtonProxy(ivToolsSC, R.drawable.tp_txsc_enable_true, R.drawable.tp_txsc_using_true, R.drawable.tp_txsc_enable_false);
        btToolsXFG = new ButtonProxy(ivToolsXFG, R.drawable.tp_fgtx_enable_true, R.drawable.tp_fgtx_using_true, R.drawable.tp_fgtx_enable_false);
        btToolsHB = new ButtonProxy(ivToolsHB, R.drawable.tp_hb_enable_true, R.drawable.tp_hb_using_true, R.drawable.tp_hb_enable_false);
        btToolsMFG = new ButtonProxy(ivToolsMFG, R.drawable.tp_gd_enable_true, R.drawable.tp_gd_using_true, R.drawable.tp_gd_enable_false);
        btToolsCX = new ButtonProxy(ivToolsCX, R.drawable.tp_txcx_enable_true, R.drawable.tp_txcx_using_true, R.drawable.tp_txcx_enable_false);
        btToolsHF = new ButtonProxy(ivToolsHF, R.drawable.tp_txhf_enable_true, R.drawable.tp_txhf_using_true, R.drawable.tp_txhf_enable_false);
        btToolsZK = new ButtonProxy(ivToolsAddHole, R.drawable.tp_add_hole_enable_true, R.drawable.tp_add_hole_using_true, R.drawable.tp_add_hole_enable_true);
        btToolsSK = new ButtonProxy(ivToolsDeleteHole, R.drawable.tp_delete_hole_true, R.drawable.tp_delete_hole_using_true, R.drawable.tp_delete_hole_true);
        btToolsBC = new ButtonProxy(ivToolsBC, R.drawable.tp_save_enable_true, R.drawable.tp_save_enable_true, R.drawable.tp_save_enable_true);
    }

    @Override
    public void onDestroyView() {
        bind.unbind();
        EventBus.getDefault().unregister(this);
    }
}
