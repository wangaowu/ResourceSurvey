package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
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
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.FieldDefn;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.srs.SpatialReferenceSystem;
import mil.nga.sf.GeometryType;

/**
 * ??????????????????GeoPackage??????
 *
 * @author gwwang
 * @date 2021/5/26 10:50
 */
@FragmentTag(name = "????????????")
public class CreateFeatureTableFragment extends BaseDialogFragment {
    private static final String TAG = "CreateFeatureTableFragment";
    private static final String[] GEOMETRY_TYPES = FieldDefn.geomTypeMap.keySet().toArray(new String[0]);

    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    private ItemController geoPackageNameItem;
    private ItemController addTabFieldItem;
    private MaterialSpinner spinnerLayerType;
    private RecyclerView recyclerView;

    private DBProject openingProject;

    //??????????????????
    private List<FieldDefn> fieldDefns = new ArrayList<>();
    //?????????????????????
    private FieldDefn primaryField = FieldDefn.create(GeoPackageQuick.COL_PRIMARY_KEY, GeoPackageDataType.INTEGER, null);
    //?????????????????????
    private FieldDefn geomField = FieldDefn.create(GeoPackageQuick.COL_GEOMETRY, GeoPackageDataType.BLOB, GeometryType.fromName(FieldDefn.geomTypeMap.get(GEOMETRY_TYPES[0])));
    private GeoPackage geoPackage;

    private CommonAsyncListener<String> createGeopackageResultListener;
    private String callbackTableName;

    //????????????????????????????????????????????????????????????
    {
        this.fieldDefns.add(primaryField);
        this.fieldDefns.add(geomField);
    }

    /**
     * ????????????
     *
     * @param openingProject ????????????
     */
    public CreateFeatureTableFragment(DBProject openingProject, CommonAsyncListener<String> createGeopackageResultListener) {
        this.openingProject = openingProject;
        this.createGeopackageResultListener = createGeopackageResultListener;
    }

    @Override
    protected void initViews(View view) {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("??????");
        appTitleController.getRightButton().setOnClickListener(v -> {
            createGeoPackageFeatureTable();
        });
        //??????
        llContainer.setGravity(Gravity.CENTER);
        DynamicItemPresenter dynamicItemPresenter = new DynamicItemPresenter(llContainer);
        //????????????
        geoPackageNameItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "????????????").editHint("????????????").content(""));
        View inflate = View.inflate(getContext(), R.layout.item_flag_spinner_layout, null);
        ((TextView) inflate.findViewById(R.id.tv_flag)).setText("????????????");
        spinnerLayerType = inflate.findViewById(R.id.spinner_field_type);
        spinnerLayerType.setItems(GEOMETRY_TYPES);
        spinnerLayerType.setOnItemSelectedListener((view1, position, id, item) -> {
            geomField.geometryType = GeometryType.fromName(FieldDefn.geomTypeMap.get(GEOMETRY_TYPES[position]));
            refreshAdapter();
        });
        dynamicItemPresenter.addItem(inflate);
        //????????????
        addTabFieldItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CENTER_ICON, "????????????").buttonText("???????????????"));
        View tableTitle = View.inflate(getContext(), R.layout.layout_vector_layers_table_tool, null);
        Resources resources = getContext().getResources();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, resources.getDimensionPixelSize(R.dimen.dpx_55));
        dynamicItemPresenter.addItem(tableTitle, layoutParams);
        //??????list
        recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dynamicItemPresenter.addItem(recyclerView, new LinearLayout.LayoutParams(-1, -1));
        //????????????
        addTabFieldItem.findButton().setBackgroundResource(R.drawable.bg_cyanotic_selector);
        addTabFieldItem.findButton().setOnClickListener(v -> {
            //???????????????????????????fieldDefn
            new VectorTableAddFieldFragment(fieldDefn -> {
                fieldDefns.add(fieldDefn);
                refreshAdapter();
            }).show(getChildFragmentManager(), "");
        });

        refreshAdapter();
    }

    private void refreshAdapter() {
        QuickList.instance().adapter(recyclerView, R.layout.item_vector_layers_table, fieldDefns.stream().filter(fieldDefn -> !(fieldDefn.fieldName.equals(GeoPackageQuick.COL_PRIMARY_KEY) || fieldDefn.fieldName.equals(GeoPackageQuick.COL_GEOMETRY))).collect(Collectors.toList()), new QuickListListener<FieldDefn>() {
            @Override
            public void onBindItem(QuickAdapter<FieldDefn> quickAdapter, SmartViewHolder h, FieldDefn field) {
                h.text(R.id.tv_field_name, field.fieldName);
                h.text(R.id.tv_field_type, FieldDefn.getFieldMapKey(field.fieldType.toString()));
                //??????????????????????????????????????????
                ImageView deleteField = (ImageView) h.findView(R.id.iv_delete_field);
                boolean notSupportDelete = GeoPackageQuick.COL_GEOMETRY.equals(field.fieldName) || GeoPackageQuick.COL_PRIMARY_KEY.equals(field.fieldName);
                deleteField.setVisibility(notSupportDelete ? View.GONE : View.VISIBLE);
                deleteField.setOnClickListener(v ->
                        new CommonConfirmDialog("??????", "???????????????????????????", new CommonAsync2Listener<CommonConfirmDialog>() {
                            @Override
                            public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                                fieldDefns.remove(field);
                                refreshAdapter();
                                commonConfirmDialog.dismiss();
                            }

                            @Override
                            public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                                commonConfirmDialog.dismiss();
                            }
                        }).show(getChildFragmentManager(), ""));
            }
        });
    }

    /**
     * ????????????featureTable
     */
    private void createGeoPackageFeatureTable() {
        String featureTableName = geoPackageNameItem.findEditText().getText().toString().trim();
        if (TextUtils.isEmpty(featureTableName)) {
            XToastUtils.info("????????????????????????!");
            return;
        }
        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.updateMessage("?????????..");
        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.show();
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            try {
                String projectGeoPackage = ProjectUtils.getProjectGeoPackage(openingProject.getName());
                geoPackage = GeoPackageQuick.connectExternalGeoPackage(projectGeoPackage);
                SpatialReferenceSystem spatialReferenceSystem = GeoPackageQuick.getRead(geoPackage).querySpatialReferenceSystem(4326);
                EditGeoPackage_ edit = new EditGeoPackage_(geoPackage);
                int effect = edit.addFeatureTableSpatialName(featureTableName, fieldDefns, spatialReferenceSystem);
                if (effect > 0) {
                    callbackTableName = featureTableName;
                }
                //????????????????????????
                OverlayRenderStyleUtils.insertOverlayConfig(featureTableName);
                //??????????????????
                GlobalInstanceHolder.mainHandler().post(() -> {
                    XToastUtils.info("????????????!");
                    GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                    dismiss();
                });
            } catch (Exception e) {
                callbackTableName = null;
                Log.e(TAG, "createNewGeoPackage ??????: " + e.getMessage());
                GlobalInstanceHolder.mainHandler().post(() -> {
                    XToastUtils.info("????????????: " + e.getMessage());
                    GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                });
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_vector_add_table_property;
    }

    @Override
    public void onDestroyView() {
        if (geoPackage != null) {
            GeoPackageQuick.sink2Database(geoPackage);
            geoPackage.close();
            createGeopackageResultListener.doSomething(callbackTableName);
        }
        super.onDestroyView();
    }
}
