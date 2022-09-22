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
 * 类功能：矢量图层管理
 *
 * @author gwwang
 * @date 2021/5/24 9:01
 */
@FragmentTag(name = "矢量图层管理")
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
                        //1.将geoPackage解析后添加到map图层
                        ProjectMapOverlayUtils.addNewEmptyOverlay(tableName);
                        MapBaseUtils.autoZoom(MapElementsHolder.getMapView());
                        //2.刷新列表
                        refreshGeoPackageLayersOfProject();
                    }
                }).show(getChildFragmentManager(), ""));
        btnExportShp.setOnClickListener(v -> showExportDetailDialog());
        //searchView
        searchView.setSearchAndClearListener(s -> {
            //搜索
            searchKey = s;
            refreshGeoPackageLayersOfProject();
        }, clear -> {
            //清空
            searchKey = null;
            refreshGeoPackageLayersOfProject();
        });
        //更新列表
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

                        //点击事件
                        //显隐图层(隐藏不可编辑)
                        holder.findView(R.id.iv_visible_layer).setOnClickListener(v -> {
                            overlay.setEnabled(!overlay.isEnabled());//该操作首页地图会主动隐藏显示,无需refresh操作
                            // 隐藏时不再编辑
                            if (!overlay.isEnabled() && isEditCurrent) {
                                MapElementsHolder.getEditableOverlays().remove(overlay);
                            }
                            adapter.notifyDataSetChanged();
                        });
                        //编辑图层(必须可见)
                        holder.findView(R.id.iv_edit_layer).setOnClickListener(v -> {
                            overlay.setEnabled(true);
                            if (isEditCurrent) {
                                //正在编辑当前图层--->不再编辑
                                MapElementsHolder.getEditableOverlays().remove(overlay);
                            } else {
                                //没有编辑当前图层--->设为编辑
                                MapElementsHolder.getEditableOverlays().add(overlay);
                            }
                            adapter.notifyDataSetChanged();
                        });
                        //缩放查看
                        holder.findView(R.id.iv_over_see_layer).setOnClickListener(v -> {
                            if (!overlay.isEnabled()) {
                                XToastUtils.info("图层不可见,无法查看!");
                                return;
                            }
                            //隐藏其他图层，仅查看自己
                            List<PackageOverlay> mapOverlays = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView());
                            for (PackageOverlay lay : mapOverlays) {
                                lay.setEnabled(lay == overlay);
                            }
                            adapter.notifyDataSetChanged();
                            //切换首页查看当前图层
                            GlobalObjectHolder.getMainActivityObject().switch2MainFragment();
                            MapElementsHolder.getMapView().zoomToBoundingBox(overlay.getBounds(), true, MapConstant.DEFAULT_BOX_PADDING);
                        });
                        //属性
                        holder.findView(R.id.iv_property_layer).setOnClickListener(v -> {
                            VectorLayerTablePropertyFragment vectorLayerTablePropertyFragment = new VectorLayerTablePropertyFragment(overlay, needReloadProjectListener -> {
                                //增删字段工程需要重新加载图层
                                if (needReloadProjectListener) {
                                    EventBus.getDefault().post(new EventCluster.EventChangeProject());
                                }
                            });
                            vectorLayerTablePropertyFragment.show(getChildFragmentManager(), "");
                        });
                        //样式
                        holder.findView(R.id.iv_decorate_layer).setOnClickListener(v -> {
                            new ConfigFeatureStyleDialog(overlay, dialog -> {
                                //更新配置之后需要以新样式重新加载图层
                                EventBus.getDefault().post(new EventCluster.EventChangeProject());
                                dialog.dismiss();
                            }).show(getChildFragmentManager(), null);
                        });
                        //移除
                        holder.findView(R.id.iv_remove_layer).setOnClickListener(v -> {
                            new CommonConfirmDialog("提示", "将会移除所处文件的关联图层", new CommonAsync2Listener<CommonConfirmDialog>() {
                                @Override
                                public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                                    overlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
                                        //1.从gpkg表里面删除对应数据
                                        new EditGeoPackage_(geoPackage).delFeatureTableSpatialName(overlay.getName());
                                        GeoPackageQuick.sink2Database(geoPackage);
                                        geoPackage.close();
                                        //2.地图移除该图层
                                        MapElementsHolder.getMapView().getOverlayManager().remove(overlay);
                                        MapElementsHolder.getMapView().invalidate();
                                        //3.移除该图层的样式配置
                                        OverlayRenderStyleUtils.deleteOverlayRenderStyle(overlay.getName());
                                        //4.如果正在编辑
                                        if (isEditCurrent) {
                                            MapElementsHolder.setCurrentEditOverlay(null);
                                        }
                                        //5.刷新list
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
            //通知数据改变
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_REQ_SHP_FILE && resultCode == Activity.RESULT_OK) {
            String projectGeoPackage = ProjectUtils.getProjectGeoPackage(GlobalObjectHolder.getOpeningProject().getName());
            String shpPath = FilePickerManager.INSTANCE.obtainData().get(0);
            LoadingDialog mLoadingDialog = GlobalObjectHolder.getMainActivityObject().mLoadingDialog;
            mLoadingDialog.updateMessage("请耐心等待,导入中..");
            mLoadingDialog.show();
            //1.导入shp文件到geopackage
            new Shp2GeoPackageImpl(shpPath, projectGeoPackage, new To84GeometryWktImpl()).execute(geoFeatureTableName -> {
                if (!TextUtils.isEmpty(geoFeatureTableName)) {
                    XToastUtils.info("导入成功!");
                    //2.插入图层渲染配置
                    OverlayRenderStyleUtils.insertOverlayConfig(geoFeatureTableName);
                    //3. 重新加载所有图层
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
