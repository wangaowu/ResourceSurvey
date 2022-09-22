package com.bytemiracle.resourcesurvey.modules.datamanage;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.BaseFragment;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.controller.DataPageController;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.FieldDict;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.adapter.SelectLayerNameAdapter;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.FieldDictProvider;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.SelectLayerName;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import butterknife.BindView;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.features.user.FeatureCursor;
import mil.nga.geopackage.features.user.FeatureDao;
import mil.nga.geopackage.features.user.FeatureRow;
import mil.nga.sf.Geometry;
import mil.nga.sf.GeometryEnvelope;
import mil.nga.sf.GeometryType;
import mil.nga.sf.Point;

/**
 * 类功能：查询统计页面
 *
 * @author gwwang
 * @date 2021/5/21 11:22
 */
@FragmentTag(name = "查询统计")
public class DataManageFragment extends BaseFragment {
    private static final String TAG = "DataManageFragment";
    private static final String STR_INIT_CONDITION = "打开条件编辑器(不填查所有)";
    private static final int ITEM_WIDTH_RATIO = 25;  //90dp
    private static final int PLACEHOLDER_WIDTH_RATIO = 8;  //40dp

    @BindView(R.id.rv_result)
    SwipeRecyclerView rvResult;
    @BindView(R.id.btn_query)
    Button btnQuery;
    @BindView(R.id.tag_fields_title)
    ViewGroup tagFieldsTitle;
    @BindView(R.id.grid_layers)
    GridView gridLayers;
    @BindView(R.id.tv_conditions)
    TextView tvConditions;
    @BindView(R.id.layout_result)
    View layoutResult;
    @BindView(R.id.layout_none_data)
    View layoutNoneData;

    private DataPageController dataPageController;

    private GeoPackage geoPackage;
    private FeatureDao layerFeatureDao;
    private int DP5;

    //占位符的布局参数
    private LinearLayout.LayoutParams placeHolderLayoutParams;
    //实际内容的布局参数
    private LinearLayout.LayoutParams contentLayoutParams;
    private int blackColor;
    private int ltGrayColor;
    private Map<String, List<DBFieldDict>> existFieldDict;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_summary_query;
    }

    @Override
    protected void initViews() {
        dataPageController = new DataPageController(layoutResult, layoutNoneData);
        DP5 = getResources().getDimensionPixelSize(R.dimen.dpx_5);
        placeHolderLayoutParams = new LinearLayout.LayoutParams(DP5 * PLACEHOLDER_WIDTH_RATIO, -1);
        contentLayoutParams = new LinearLayout.LayoutParams(DP5 * ITEM_WIDTH_RATIO, -1);
        blackColor = getResources().getColor(R.color.black);
        ltGrayColor = getResources().getColor(R.color.gray_2_divider);

        tvConditions.setOnClickListener(v -> {
            dataPageController.showNoneView("请点击查询");
            if (layerFeatureDao != null) {
                List<String> columnNames = layerFeatureDao.getColumns().stream()
                        .filter(featureColumn -> !featureColumn.isGeometry())
                        .map(featureColumn -> featureColumn.getName())
                        .collect(Collectors.toList());
                new com.bytemiracle.resourcesurvey.modules.summaryquery.ConditionsDialog().setDialogDataListener(columnNames, buildConditions -> {
                    tvConditions.setText(buildConditions);
                }).show(getChildFragmentManager(), "");
            } else {
                XToastUtils.info("请先选择图层!", Toast.LENGTH_LONG);
            }
        });
        btnQuery.setOnClickListener(v -> prepareQuery());

        tvConditions.setText(STR_INIT_CONDITION);
        dataPageController.showNoneView("请点击查询");
        openGeoPackage(geoPackage_ -> {
            geoPackage = geoPackage_;
            initLayerNameItems(geoPackage.getFeatureTables());
        });
    }

    @Override
    protected void initViewsData() {
        existFieldDict = FieldDictProvider.getFieldDict(getContext(), GlobalObjectHolder.getOpeningProject().getId());
    }

    private void setRowsTitle(List<CheckableFeatureColumn> sheetColumns, List<FeatureRow> featureRows) {
        LinearLayout llFieldsTitle = tagFieldsTitle.findViewById(R.id.ll_preview_container);
        llFieldsTitle.removeAllViews();
        ImageView ivFilter = new ImageView(getContext());
        ivFilter.setPadding(DP5, DP5, DP5, DP5);
        ivFilter.setImageResource(R.drawable.ic_filter);
        llFieldsTitle.addView(ivFilter, placeHolderLayoutParams);
        for (CheckableFeatureColumn column : sheetColumns) {
            if (column.isChecked()) {
                TextView item = new TextView(getContext());
                item.setGravity(Gravity.CENTER);
                item.getPaint().setFakeBoldText(true);
                item.setTextColor(blackColor);
                item.setBackgroundColor(ltGrayColor);
                item.setText(column.data.getName());
                llFieldsTitle.addView(item, contentLayoutParams);
            }
        }
        ivFilter.setOnClickListener(v -> {
            showFilterColumnsDialog(sheetColumns, onCompleted -> setRowsAdapter(sheetColumns, featureRows));
        });
    }

    private void showFilterColumnsDialog(List<CheckableFeatureColumn> sheetColumns, CommonAsyncListener<Boolean> onFilterCompletedListener) {
        List<String> layerColumnNames = sheetColumns.stream()
                .map(featureColumn -> featureColumn.data.getName())
                .collect(Collectors.toList());
        String[] layerNameArray = new String[layerColumnNames.size()];
        layerNameArray = layerColumnNames.toArray(layerNameArray);

        boolean[] checkedColumns = new boolean[sheetColumns.size()];
        for (int i = 0; i < checkedColumns.length; i++) {
            checkedColumns[i] = sheetColumns.get(i).isChecked();
        }
        new AlertDialog.Builder(getContext())
                .setMultiChoiceItems(layerNameArray, checkedColumns,
                        (dialogInterface, position, checked) -> {
                            sheetColumns.get(position).setChecked(checked);
                        })
                .setTitle("请选中要展示的列")
                .setPositiveButton("确认", (dialog, i) -> {
                    onFilterCompletedListener.doSomething(true);
                    dialog.dismiss();
                }).create().show();
    }

    private void prepareQuery() {
        if (layerFeatureDao == null) {
            XToastUtils.info("请选择要查询的图层名称!");
            return;
        }
        String conditions = tvConditions.getText().toString();
        if (STR_INIT_CONDITION.equals(conditions)) {
            conditions = " 1 = 1";
        }
        String finalConditions = conditions;
        dataPageController.showLoadingView();
        List<CheckableFeatureColumn> sheetColumns = layerFeatureDao.getColumns().stream()
                .filter(featureColumn -> !featureColumn.isGeometry()) //永远忽略图形列
                .map(featureColumn -> new CheckableFeatureColumn(featureColumn, true))
                .collect(Collectors.toList());
        GlobalInstanceHolder.mainHandler().postDelayed(() -> {
            List<FeatureRow> featureRows = queryFeatureRows(finalConditions);
            setRowsAdapter(sheetColumns, featureRows);
        }, 1000);
    }

    private List<FeatureRow> queryFeatureRows(String conditions) {
        List<FeatureRow> featureRows = new ArrayList<>();
        try {
            FeatureCursor featureCursor = layerFeatureDao.query(conditions);
            if (featureCursor.getCount() == 0) {
                dataPageController.showNoneView("查询结果为空!");
                return null;
            }
            while (featureCursor.moveToNext()) {
                featureRows.add(featureCursor.getRow());
            }
        } catch (Exception e) {
            dataPageController.showNoneView("请检查查询条件正确性!");
        } finally {
            return featureRows;
        }
    }

    private void setRowsAdapter(List<CheckableFeatureColumn> sheetColumns, List<FeatureRow> featureRows) {
        if (ListUtils.isEmpty(featureRows)) {
            dataPageController.showNoneView("查询为空!");
            return;
        }
        dataPageController.showNoneView(false);
        setRowsTitle(sheetColumns, featureRows);
        QuickList.instance().adapter(rvResult, R.layout.layout_preview_feature_row, featureRows, new QuickListListener<FeatureRow>() {
            @Override
            public void onBindItem(QuickAdapter<FeatureRow> quickAdapter, SmartViewHolder smartViewHolder, FeatureRow featureRow) {
                LinearLayout llPreviewContainer = (LinearLayout) smartViewHolder.findView(R.id.ll_preview_container);
                //第一列（title为过滤标记）的占位符
                View placeHolder = new View(getContext());
                llPreviewContainer.addView(placeHolder, placeHolderLayoutParams);
                for (CheckableFeatureColumn column : sheetColumns) {
                    if (column.isChecked()) {
                        String columnName = column.data.getName();
                        TextView tv = new TextView(getContext());
                        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                        Object value = featureRow.getValue(columnName);
                        tv.setText(value == null ? "空" : String.valueOf(value));
                        tv.setOnClickListener(v -> showValueDialog(columnName, value));
                        llPreviewContainer.addView(tv, contentLayoutParams);
                    }
                }
                placeHolder.setOnClickListener(view -> {
                    Geometry geometry = featureRow.getGeometry().getGeometry();
                    if (!geometry.isEmpty()) {
                        switch2MapView(geometry);
                    } else {
                        XToastUtils.info("图形数据异常，无法跳转!");
                    }
                });
            }
        });
    }

    private void showValueDialog(String columnName, Object value) {
        if (value instanceof String && existFieldDict != null) {
            List<DBFieldDict> dbFieldDicts = existFieldDict.get(layerFeatureDao.getTableName());
            DBFieldDict fieldDict = FieldDictProvider.getFieldDict(columnName, dbFieldDicts);
            if (fieldDict == null) {
                XToastUtils.info("值已显示,与字典无关!");
                return;
            }
            FieldDict.Pair[] pairs = JsonParser.fromJson(fieldDict.getFieldValuePool(), FieldDict.Pair[].class);
            if (pairs == null) {
                XToastUtils.info("值已显示,与字典无关!");
                return;
            }
            String showMessage = FieldDict.getValueArrayString(pairs, (String) value);
            new AlertDialog.Builder(getContext())
                    .setTitle(columnName)
                    .setMessage(showMessage)
                    .show();
        } else {
            XToastUtils.info("值已显示,与字典无关!");
        }
    }

    //切换首页查看当前要素
    private void switch2MapView(Geometry geometry) {
        GeometryType geometryType = geometry.getGeometryType();
        switch (geometryType) {
            case POLYGON:
            case MULTIPOLYGON:
                //BoundingBox constructor :  double north, final double east, final double south, final double west
                GeometryEnvelope envelope = geometry.getEnvelope();
                double north = envelope.getMaxY();
                double south = envelope.getMinY();
                double east = envelope.getMaxX();
                double west = envelope.getMinX();
                BoundingBox boundingBox = new BoundingBox(north, east, south, west);
                GlobalObjectHolder.getMainActivityObject().switch2MainFragment();
                MapElementsHolder.getMapView().zoomToBoundingBox(boundingBox, true, MapConstant.DEFAULT_BOX_PADDING);
                break;
            case MULTIPOINT:
            case MULTILINESTRING:
            case POINT:
            case LINESTRING:
                Point centroid = geometry.getCentroid();
                GlobalObjectHolder.getMainActivityObject().switch2MainFragment();
                MapElementsHolder.getMapView().setExpectedCenter(new GeoPoint(centroid.getY(), centroid.getX()));
                break;
        }
    }

    private void initLayerNameItems(List<String> overlayNames_) {
        List<SelectLayerName> layers = overlayNames_.stream()
                .map(s -> new SelectLayerName(s, false))
                .collect(Collectors.toList());
        //默认选中第一个图层名称
        layers.get(0).setChecked(true);
        layerFeatureDao = geoPackage.getFeatureDao(layers.get(0).name);
        gridLayers.setAdapter(new SelectLayerNameAdapter(getContext(), layers, selectedLayer -> {
            layerFeatureDao = geoPackage.getFeatureDao(selectedLayer);
            tvConditions.setText(STR_INIT_CONDITION);
            dataPageController.showNoneView("请点击查询");
        }));
    }

    private void openGeoPackage(CommonAsyncListener<GeoPackage> geoPackageResultListener) {
        mLoadingDialog.updateMessage("初始化信息中");
        mLoadingDialog.show();
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            String openingProjectName = GlobalObjectHolder.getOpeningProject().getName();
            String projectGeoPackage = ProjectUtils.getProjectGeoPackage(openingProjectName);
            GeoPackage geoPackage = GeoPackageQuick.connectExternalGeoPackage(projectGeoPackage);
            GlobalInstanceHolder.mainHandler().post(() -> {
                mLoadingDialog.dismiss();
                geoPackageResultListener.doSomething(geoPackage);
            });
        });
    }
}
