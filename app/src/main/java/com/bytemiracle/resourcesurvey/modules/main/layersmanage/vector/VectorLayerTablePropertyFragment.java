package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.dynamicitem.DynamicItemPresenter;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemController;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemData;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.FieldDictProvider;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.FieldDefn;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.overlay.render.PackageOverlay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureDao;

/**
 * 类功能：矢量图层当前图层信息
 *
 * @author gwwang
 * @date 2021/5/26 10:50
 */
@FragmentTag(name = "图层信息")
public class VectorLayerTablePropertyFragment extends BaseDialogFragment {
    private static final String TAG = "VectorLayerTablePropertyFragment";

    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;

    private ItemController layerNameItem;
    private ItemController layerTypeItem;
    private ItemController fieldDictItem;
    private ItemController addFieldItem;

    private List<DBFieldDict> dbFieldDicts;

    private PackageOverlay overlay;
    private GeoPackage geoPackage;
    private CommonAsyncListener<Boolean> needReloadProjectListener;

    /**
     * 构造方法
     *
     * @param overlay                   图层对象
     * @param needReloadProjectListener 需要重新加载工程的监听器(增/删字段)
     */
    public VectorLayerTablePropertyFragment(PackageOverlay overlay, CommonAsyncListener<Boolean> needReloadProjectListener) {
        this.overlay = overlay;
        this.needReloadProjectListener = needReloadProjectListener;
    }

    @Override
    protected void initViews(View view) {
        //内容
        llContainer.setGravity(Gravity.CENTER);
        DynamicItemPresenter dynamicItemPresenter = new DynamicItemPresenter(llContainer);
        layerNameItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "图层名称").editHint("图层名称").content(""));
        layerTypeItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "图层类型").content("", false));
        fieldDictItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CENTER_ICON, "字段字典").buttonText("配置"));

        addFieldItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CENTER_ICON, "表字段").buttonText("添加"));

        refreshItems();
        refreshAdapter();
    }

    private void refreshAdapter() {
        dbFieldDicts = FieldDictProvider.getFieldDict(getContext(), GlobalObjectHolder.getOpeningProject().getId()).get(overlay.getName());
        overlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage_ -> {
            geoPackage = geoPackage_;
            FeatureDao featureDao = geoPackage.getFeatureDao(overlay.getName());
            List<FeatureColumn> featureColumns = featureDao.getColumns().stream()
                    .filter(featureColumn -> !featureColumn.isGeometry() && !featureColumn.isPrimaryKey())
                    .collect(Collectors.toList());
            layerTypeItem.findTextView().setText(FieldDefn.getGeomTypeMapKey(featureDao.getGeometryType().getName()));
            setAdapterData(featureColumns);
        });
    }

    private void setAdapterData(List<FeatureColumn> featureColumns) {
        QuickList.instance().adapter(rvContent, R.layout.item_vector_layers_table, featureColumns,
                new QuickListListener<FeatureColumn>() {
                    @Override
                    public void onBindItem(QuickAdapter<FeatureColumn> quickAdapter, SmartViewHolder h, FeatureColumn featureColumn) {
                        String columnName = featureColumn.getName();
                        String cnType = FieldDefn.getFieldMapKey(featureColumn.getDataType().toString());
                        h.text(R.id.tv_field_name, columnName);
                        h.text(R.id.tv_field_type, FieldDictProvider.getTypeWithCheckType(columnName, cnType, dbFieldDicts));
                        //默认字段和图形字段不支持删除
                        ImageView deleteField = (ImageView) h.findView(R.id.iv_delete_field);
                        boolean notSupportDelete = GeoPackageQuick.COL_GEOMETRY.equals(columnName) || GeoPackageQuick.COL_PRIMARY_KEY.equals(columnName);
                        deleteField.setVisibility(notSupportDelete ? View.GONE : View.VISIBLE);
                        //删除字段
                        deleteField.setOnClickListener(v -> {
                            new CommonConfirmDialog("提示", "将同步删除该列数据", new CommonAsync2Listener<CommonConfirmDialog>() {
                                @Override
                                public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                                    try {
                                        new EditGeoPackage_(geoPackage).delFeatureTableCol(overlay.getName(), columnName);
                                        refreshAdapter();
                                        needReloadProjectListener.doSomething(true);
                                        commonConfirmDialog.dismiss();
                                    } catch (Exception e) {
                                        XToastUtils.info("删除字段失败: " + e.getMessage());
                                    }
                                }

                                @Override
                                public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                                    commonConfirmDialog.dismiss();
                                }
                            }).show(getChildFragmentManager(), "");
                        });
                    }
                });
    }

    private void refreshItems() {
        layerNameItem.findEditText().setText(overlay.getName());
        //不让编辑
        layerNameItem.lockItem();
        //字段字典配置
        fieldDictItem.findButton().setBackgroundResource(R.drawable.bg_cyanotic_selector);
        fieldDictItem.findButton().setOnClickListener(v ->
                //配置字典
                new ConfigLayerFieldPoolFragment(Arrays.asList(overlay.getName()), onDismiss -> refreshAdapter())
                        .show(getChildFragmentManager(), ""));
        //添加表字段
        addFieldItem.findButton().setBackgroundResource(R.drawable.bg_cyanotic_selector);
        addFieldItem.findButton().setOnClickListener(v ->
                //从创建字段界面创建fieldDefn
                new VectorTableAddFieldFragment(fieldDefn -> {
                    try {
                        if (new EditGeoPackage_(geoPackage).addFeatureTableCol(overlay.getName(), fieldDefn)) {
                            XToastUtils.info("新增成功!");
                            refreshAdapter();
                            needReloadProjectListener.doSomething(true);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "addFeatureTableCol 失败: " + e.getMessage());
                        XToastUtils.info("新增失败!");
                    }
                }).show(getChildFragmentManager(), ""));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_vector_layer_table_property;
    }

    @Override
    public void onDestroy() {
        GeoPackageQuick.sink2Database(geoPackage);
        super.onDestroy();
    }
}
