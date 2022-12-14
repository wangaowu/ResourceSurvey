package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.base.framework.view.search.AppSearchView;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.bytemiracle.resourcesurvey.common.viewutil.RecyclerViewItemDrag;
import com.bytemiracle.resourcesurvey.modules.settings.ConfigFeatureStyleDialog;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.xuexiang.xui.widget.dialog.LoadingDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.customImpl.convert.GeoPackage2ShpImpl;
import org.osmdroid.customImpl.convert.Shp2GeoPackageImpl;
import org.osmdroid.customImpl.convert.transform.To84GeometryWktImpl;
import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.customImpl.shp.Shp_;
import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapBaseUtils;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.overlay.utils.MapOverlayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import me.rosuh.filepicker.config.FilePickerManager;

/**
 * ??????????????????????????????
 *
 * @author gwwang
 * @date 2021/5/24 9:01
 */
@FragmentTag(name = "??????????????????")
@RequiresApi(api = Build.VERSION_CODES.N)
public class VectorLayerManageFragment extends BaseDialogFragment {
    private static final String TAG = "F_VectorLayerManage";
    private static final int CODE_REQ_SHP_FILE = 1;

    @BindView(R.id.btn_import)
    Button btnImportShp;
    @BindView(R.id.btn_export)
    Button btnExportShp;
    @BindView(R.id.btn_create)
    Button btnCreate;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.search_view)
    AppSearchView searchView;

    private String searchKey;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventCluster.EventAllLayersLoaded event) {
        resetAdapterList();
    }

    private void resetAdapterList() {
        searchKey = null;
        ((TextView) Shp_.GeometryUtils.invokeGet(searchView, "etContent")).setText(searchKey);
        refreshGeoPackageLayersOfProject();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_vector_layer_manage;
    }

    @Override
    protected void initViews(View view) {
        EventBus.getDefault().register(this);
        btnImportShp.setOnClickListener(v -> FilePickerManager.from(this)
                .enableSingleChoice()
                .forResult(CODE_REQ_SHP_FILE));
        btnCreate.setOnClickListener(v ->
                new CreateFeatureTableFragment(GlobalObjectHolder.getOpeningProject(), tableName -> {
                    if (tableName != null) {
                        //1.???geoPackage??????????????????map??????
                        ProjectMapOverlayUtils.addNewEmptyOverlay(tableName);
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                        //2.????????????
                        refreshGeoPackageLayersOfProject();
                    }
                }).show(getChildFragmentManager(), ""));
        btnExportShp.setOnClickListener(v -> showExportDetailDialog());
        //searchView
        searchView.setSearchAndClearListener(s -> {
            //??????
            searchKey = s;
            refreshGeoPackageLayersOfProject();
        }, clear -> {
            //??????
            searchKey = null;
            refreshGeoPackageLayersOfProject();
        });
        //????????????
        refreshGeoPackageLayersOfProject();
    }

    private void showExportDetailDialog() {
        DetailScrollInfoDialog detailScrollInfoDialog = new DetailScrollInfoDialog();
        detailScrollInfoDialog.show(getChildFragmentManager(), "");
        String projectName = GlobalObjectHolder.getOpeningProject().getName();
        String projectGeoPackage = ProjectUtils.getProjectGeoPackage(projectName);
        String exportDir = ProjectUtils.getProjectExportDir(projectName);
        String prjWkt = GlobalObjectHolder.getOpeningProject().getPrjWKT();
        new GeoPackage2ShpImpl(projectGeoPackage).execute(exportDir, prjWkt, lineContent ->
                GlobalInstanceHolder.mainHandler().post(() -> detailScrollInfoDialog.appendInfo(lineContent)));
    }

    private void refreshGeoPackageLayersOfProject() {
        List<PackageOverlay> mapOverlays = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView());
        if (ListUtils.isEmpty(mapOverlays)) {
            setLayerInfoList(new ArrayList<>());
            return;
        }
        List<PackageOverlay> overlays = mapOverlays.stream()
                .filter(overlay -> {
                    if (TextUtils.isEmpty(searchKey)) {
                        return true;
                    }
                    return overlay.getName().contains(searchKey);
                })
                .collect(Collectors.toList());
        setLayerInfoList(overlays);
    }

    private void setLayerInfoList(List<PackageOverlay> allOverlays) {
        QuickAdapter adapter = QuickList.instance().adapter(rvContent, R.layout.item_vector_function_layers, allOverlays
                , new QuickListListener<PackageOverlay>() {
                    @Override
                    public void onBindItem(QuickAdapter<PackageOverlay> adapter, SmartViewHolder holder, PackageOverlay overlay) {
                        holder.text(R.id.tv_layer_name, overlay.getName());
                        holder.text(R.id.tv_prj_system, GlobalObjectHolder.getOpeningProject().getSpatialReferenceSimpleName());
                        holder.image(R.id.iv_visible_layer, overlay.isEnabled() ? R.drawable.ic_visible_layer : R.drawable.ic_invisible_layer);
                        holder.image(R.id.iv_over_see_layer, overlay.isEnabled() ? R.drawable.ic_can_oversee : R.drawable.ic_no_oversee);
                        boolean isEditCurrent = MapElementsHolder.getEditableOverlays().contains(overlay);
                        holder.image(R.id.iv_edit_layer, isEditCurrent ? R.drawable.ic_can_edit_layer : R.drawable.ic_no_edit_layer);

                        //????????????
                        //????????????(??????????????????)
                        holder.findView(R.id.iv_visible_layer).setOnClickListener(v -> {
                            overlay.setEnabled(!overlay.isEnabled());//??????????????????????????????????????????,??????refresh??????
                            // ?????????????????????
                            if (!overlay.isEnabled() && isEditCurrent) {
                                MapElementsHolder.getEditableOverlays().remove(overlay);
                            }
                            adapter.notifyDataSetChanged();
                        });
                        //????????????(????????????)
                        holder.findView(R.id.iv_edit_layer).setOnClickListener(v -> {
                            overlay.setEnabled(true);
                            if (isEditCurrent) {
                                //????????????????????????--->????????????
                                MapElementsHolder.getEditableOverlays().remove(overlay);
                            } else {
                                //????????????????????????--->????????????
                                MapElementsHolder.getEditableOverlays().add(overlay);
                            }
                            adapter.notifyDataSetChanged();
                        });
                        //????????????
                        holder.findView(R.id.iv_over_see_layer).setOnClickListener(v -> {
                            if (!overlay.isEnabled()) {
                                XToastUtils.info("???????????????,????????????!");
                                return;
                            }
                            //????????????????????????????????????
                            List<PackageOverlay> mapOverlays = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView());
                            for (PackageOverlay lay : mapOverlays) {
                                lay.setEnabled(lay == overlay);
                            }
                            adapter.notifyDataSetChanged();
                            //??????????????????????????????
                            GlobalObjectHolder.getMainActivityObject().switch2MainFragment();
                            MapElementsHolder.getMapView().zoomToBoundingBox(overlay.getBounds(), true, MapConstant.DEFAULT_BOX_PADDING);
                        });
                        //??????
                        holder.findView(R.id.iv_property_layer).setOnClickListener(v -> {
                            VectorLayerTablePropertyFragment vectorLayerTablePropertyFragment = new VectorLayerTablePropertyFragment(overlay, needReloadProjectListener -> {
                                //??????????????????????????????????????????
                                if (needReloadProjectListener) {
                                    EventBus.getDefault().post(new EventCluster.EventChangeProject());
                                }
                            });
                            vectorLayerTablePropertyFragment.show(getChildFragmentManager(), "");
                        });
                        //??????
                        holder.findView(R.id.iv_decorate_layer).setOnClickListener(v -> {
                            new ConfigFeatureStyleDialog(overlay, dialog -> {
                                //??????????????????????????????????????????????????????
                                EventBus.getDefault().post(new EventCluster.EventChangeProject());
                                dialog.dismiss();
                            }).show(getChildFragmentManager(), null);
                        });
                        //??????
                        holder.findView(R.id.iv_remove_layer).setOnClickListener(v -> {
                            new CommonConfirmDialog("??????", "???????????????????????????????????????", new CommonAsync2Listener<CommonConfirmDialog>() {
                                @Override
                                public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                                    overlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
                                        //1.???gpkg???????????????????????????
                                        new EditGeoPackage_(geoPackage).delFeatureTableSpatialName(overlay.getName());
                                        GeoPackageQuick.sink2Database(geoPackage);
                                        geoPackage.close();
                                        //2.?????????????????????
                                        MapElementsHolder.getMapView().getOverlayManager().remove(overlay);
                                        MapElementsHolder.getMapView().invalidate();
                                        //3.??????????????????????????????
                                        OverlayRenderStyleUtils.deleteOverlayRenderStyle(overlay.getName());
                                        //4.??????????????????
                                        if (isEditCurrent) {
                                            MapElementsHolder.setCurrentEditOverlay(null);
                                        }
                                        //5.??????list
                                        GlobalInstanceHolder.mainHandler().post(() -> {
                                            refreshGeoPackageLayersOfProject();
                                            commonConfirmDialog.dismiss();
                                        });
                                    });
                                }

                                @Override
                                public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                                    commonConfirmDialog.dismiss();
                                }
                            }).show(getChildFragmentManager(), "");
                        });
                    }
                });
        RecyclerViewItemDrag.attach(rvContent, adapter.getListData(), l -> {
            //??????????????????
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQ_SHP_FILE && resultCode == Activity.RESULT_OK) {
            String projectGeoPackage = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
            String shpPath = FilePickerManager.INSTANCE.obtainData().get(0);
            LoadingDialog mLoadingDialog = GlobalObjectHolder.getMainActivityObject().mLoadingDialog;
            mLoadingDialog.updateMessage("???????????????,?????????..");
            mLoadingDialog.show();
            //1.??????shp?????????geopackage
            new Shp2GeoPackageImpl(shpPath, projectGeoPackage, new To84GeometryWktImpl()).execute(geoFeatureTableName -> {
                if (!TextUtils.isEmpty(geoFeatureTableName)) {
                    XToastUtils.info("????????????!");
                    //2.????????????????????????
                    OverlayRenderStyleUtils.insertOverlayConfig(geoFeatureTableName);
                    //3. ????????????????????????
                    ProjectMapOverlayUtils.reloadAllFeatures(mLoadingDialog, boundingBox -> {
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                        GlobalInstanceHolder.mainHandler().post(() -> {
                            mLoadingDialog.dismiss();
                            refreshGeoPackageLayersOfProject();
                        });
                    });
                }
            });
        }
    }


    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
