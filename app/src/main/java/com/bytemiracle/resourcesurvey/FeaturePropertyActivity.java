package com.bytemiracle.resourcesurvey;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.fragment.dynamicitem.DynamicItemPresenter;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemController;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemData;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.view.ItemView;
import com.bytemiracle.resourcesurvey.common.view.ItemViewData;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.FieldDictProvider;
import com.bytemiracle.resourcesurvey.modules.media.MediaPropertyFragment;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.osmdroid.customImpl.geopackage.EditGeoPackage_;
import org.osmdroid.customImpl.geopackage.GeoPackageQuick;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.db.GeoPackageDataType;
import mil.nga.geopackage.features.user.FeatureColumn;
import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能： feature属性展示界面
 *
 * @author gwwang
 * @date 2021/6/8 15:14
 */
public class FeaturePropertyActivity extends BaseActivity {
    private static final String TAG = "FeaturePropertyActivity";

    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    @BindView(R.id.layout_media)
    View layoutMedia;

    private DynamicItemPresenter dynamicItemPresenter;
    private List<ItemController> itemControllers = new ArrayList<>();
    private List<View> itemViews = new ArrayList<>();

    private GeoPackage geoPackage;
    private OverlayWithIW identifyShape;
    private FeatureOverlayInfo featureOverlayInfo;
    private FeatureRow featureRow;
    private List<DBFieldDict> dbFieldDicts;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_feature_property;
    }

    @Override
    protected void initViewsWithSavedInstanceState(Bundle savedInstanceState) {
        identifyShape = MapElementsHolder.getIdentifyShape();
        featureOverlayInfo = ((ISelectOverlay) identifyShape).getFeatureOverlayInfo();
        featureRow = featureOverlayInfo.getFeatureRow();

        initTopBarListener();
        layoutMedia.setOnClickListener(this::showMediaPropertyDialog);
        dynamicItemPresenter = new DynamicItemPresenter(llContainer);

        String overlayName = featureOverlayInfo.getPackageOverlay().getName();
        dbFieldDicts = FieldDictProvider.getFieldDict(this, GlobalObjectHolder.getOpeningProject().getId()).get(overlayName);

        featureOverlayInfo.getPackageOverlay().getPackageOverlayInfo().openWritableGeoPackage(geoPackage_ -> {
            geoPackage = geoPackage_;
            Pair<List<FeatureColumn>, FeatureRow> properties = readFeatureTableProperties();
            //展示属性字段/值数据
            showProperties(properties);
        });
    }

    private void initTopBarListener() {
        getBarRightButton().setVisibility(View.VISIBLE);
        getBarRightButton().setBackgroundResource(R.drawable.bg_app_text_radius_5_gray_selector);
        getBarRightButton().setTextColor(getColor(R.color.common_dark));
        getBarRightButton().setText("保存");
        getBarRightButton().setOnClickListener(v -> {
            //读取ui的属性
            Map<String, Object> uiProperties = readPropertiesFromUI();
            FeatureRow newFeatureRow = updateRowUseUI(featureRow, uiProperties);
            GlobalInstanceHolder.newSingleExecutor().execute(() -> {
                //保存
                long[] effectCount = new long[]{0L};
                try {
                    effectCount[0] = new EditGeoPackage_(geoPackage).updateFeature(newFeatureRow);
                } catch (Exception e) {
                    Log.e(TAG, "updateFeature failed: " + e.toString());
                } finally {
                    GlobalInstanceHolder.mainHandler().post(() -> {
                        if (effectCount[0] == 1) {
                            ((ISelectOverlay) MapElementsHolder.getIdentifyShape()).getFeatureOverlayInfo().setFeatureRow(newFeatureRow);
                            XToastUtils.info("保存成功");
                        } else {
                            XToastUtils.info("保存失败:" + effectCount[0]);
                        }
                    });
                }
            });
        });
    }

    private Pair<List<FeatureColumn>, FeatureRow> readFeatureTableProperties() {
        FeatureOverlayInfo featureOverlayInfo = ((ISelectOverlay) identifyShape).getFeatureOverlayInfo();
        FeatureRow featureRow = featureOverlayInfo.getFeatureRow();
        List<FeatureColumn> featureColumns = featureRow.getColumns().getColumns();
        return new Pair(featureColumns, featureRow);
    }

    private void showProperties(Pair<List<FeatureColumn>, FeatureRow> properties) {
        llContainer.removeAllViews();
        List<FeatureColumn> featureColumns = properties.first;
        FeatureRow featureRow = properties.second;
        for (FeatureColumn featureColumn : featureColumns) {
            if (featureColumn.isPrimaryKey() || featureColumn.isGeometry()) {
                continue;
            }
            String fieldName = featureColumn.getName();
            Object value = featureRow.getValue(fieldName);
            DBFieldDict fieldDict = FieldDictProvider.getFieldDict(fieldName, dbFieldDicts);
            int checkType = fieldDict == null ? DBFieldDict.TYPE_INPUT_CHECK : fieldDict.getCheckType();
            String fieldNameWithCheckType = FieldDictProvider.getNameWithCheckType(fieldName, checkType);
            if (checkType == DBFieldDict.TYPE_SINGLE_CHECK) {
                //单选
                ItemViewData itemViewData = new ItemViewData(fieldName, checkType, fieldDict.getFieldValuePool());
                itemViewData.setStringVal(String.valueOf(value == null ? "" : value));
                itemViewData.setAliasName(fieldNameWithCheckType);
                View view = ItemView.addSingleCheckItem(llContainer,
                        itemViewData,
                        getSupportFragmentManager());
                view.setTag(featureColumn);
                itemViews.add(view);
                continue;
            } else if (checkType == DBFieldDict.TYPE_MULTI_CHECK) {
                //多选
                ItemViewData itemViewData = new ItemViewData(fieldName, checkType, fieldDict.getFieldValuePool());
                itemViewData.setStringVal(String.valueOf(value == null ? "" : value));
                itemViewData.setAliasName(fieldNameWithCheckType);
                View view = ItemView.addMultiCheckItem(llContainer,
                        itemViewData,
                        getSupportFragmentManager());
                view.setTag(featureColumn);
                itemViews.add(view);
                continue;
            }
            //checkType == DBFieldDict.TYPE_INPUT_CHECK
            GeoPackageDataType fieldType = featureColumn.getDataType();
            if (fieldType == GeoPackageDataType.DATE) {
                View view = ItemView.addDateItem(llContainer, new ItemViewData(fieldNameWithCheckType, (Date) value));
                view.setTag(featureColumn);
                itemViews.add(view);
            } else if (fieldType == GeoPackageDataType.TEXT) {
                String fieldValue = String.valueOf(value == null ? "" : value);
                ItemController itemController = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, fieldNameWithCheckType).content(fieldValue));
                itemController.itemView().setTag(featureColumn);
                itemControllers.add(itemController);
            } else if (fieldType == GeoPackageDataType.BOOLEAN) {
                View view = ItemView.addRadioItem(llContainer, new ItemViewData(fieldNameWithCheckType, ItemViewData.getBol(value)));
                view.setTag(featureColumn);
                itemViews.add(view);
            } else if (fieldType == GeoPackageDataType.INTEGER) {
                View view = ItemView.addNumberItem(llContainer, new ItemViewData(fieldNameWithCheckType, ItemViewData.getInt(value), "integer"));
                view.setTag(featureColumn);
                itemViews.add(view);
            } else if (fieldType == GeoPackageDataType.DOUBLE) {
                View view = ItemView.addNumberItem(llContainer, new ItemViewData(fieldNameWithCheckType, ItemViewData.getDouble(value), "double"));
                view.setTag(featureColumn);
                itemViews.add(view);
            } else if (fieldType == GeoPackageDataType.DATETIME) {
                //日期时间类型
                int color = getResources().getColor(R.color.common_dark);
                ItemData itemData = new ItemData(ItemData.ItemType.SELECT_DATE, fieldNameWithCheckType).dialogMainColor(color)
                        .convertDateListener(date -> AppTime.formatDateTime(date));
                Date fieldValue = (Date) value;
                if (fieldValue != null) {
                    itemData.date(fieldValue);
                }
                //将原始信息绑定到条目上
                ItemController itemController = dynamicItemPresenter.addItem(itemData);
                itemController.itemView().setTag(featureColumn);
                itemControllers.add(itemController);
            } else {
                //其他类型：简单EditText编辑，迭代 else if 分支具体实现
                String fieldValue = String.valueOf(value == null ? "" : value);
                ItemData itemData = new ItemData(ItemData.ItemType.CONTENT, fieldNameWithCheckType).content(fieldValue);
                //将原始信息绑定到条目上
                ItemController itemController = dynamicItemPresenter.addItem(itemData);
                itemController.itemView().setTag(featureColumn);
                itemControllers.add(itemController);
            }
        }
    }

    private Map<String, Object> readPropertiesFromUI() {
        Map<String, Object> fvMap = new HashMap<>();
        for (ItemController itemController : itemControllers) {
            TextView tvFlag = itemController.itemView().findViewById(R.id.tv_flag);
            String fieldName = tvFlag.getText().toString();
            GeoPackageDataType fieldType = ((FeatureColumn) itemController.itemView().getTag()).getDataType();
            if (fieldType == GeoPackageDataType.DATETIME) {
                //时间选择器
                fvMap.put(fieldName, itemController.findTextView().getTag());
            } else if (itemController.findEditText() != null) {
                //有编辑输入框
                String fieldValue = itemController.findEditText().getText().toString();
                switch (fieldType) {
                    //值类型
                    case INT:
                    case INTEGER:
                    case MEDIUMINT:
                    case DOUBLE:
                    case FLOAT:
                    case BOOLEAN:
                    case TEXT:
                        fvMap.put(fieldName, invokeValue(fieldType.getClassType(), "valueOf", fieldValue));
                        break;
                    case BLOB:
                        //图形不做处理
                        break;
                    case SMALLINT:
                    case TINYINT:
                        //其他类型暂不做处理
                        break;
                }
            }
        }

        for (View view : itemViews) {
            String fieldName = ((FeatureColumn) view.getTag()).getName();
            GeoPackageDataType fieldType = ((FeatureColumn) view.getTag()).getDataType();
            if (fieldType == GeoPackageDataType.DATE) {
                fvMap.put(fieldName, view.findViewById(R.id.tv_content).getTag());
            } else if (fieldType == GeoPackageDataType.DOUBLE || fieldType == GeoPackageDataType.INTEGER) {
                String fieldValue = ((EditText) view.findViewById(R.id.et_content)).getText().toString();
                fvMap.put(fieldName, invokeValue(fieldType.getClassType(), "valueOf", fieldValue));
            } else if (fieldType == GeoPackageDataType.TEXT) {
                // 列表单选和多选
                fvMap.put(fieldName, view.findViewById(R.id.tv_content).getTag());
            } else if (fieldType == GeoPackageDataType.BOOLEAN) {
                String fieldValue = "false";
                if (R.id.yes == ((RadioGroup) view.findViewById(R.id.radio_group)).getCheckedRadioButtonId()) {
                    fieldValue = "true";
                }
                fvMap.put(fieldName, invokeValue(fieldType.getClassType(), "valueOf", fieldValue));
            }
        }
        return fvMap;
    }

    private FeatureRow updateRowUseUI(FeatureRow featureRow, Map<String, Object> uiProperties) {
        FeatureRow newFeatureRow = new FeatureRow(featureRow);
        if (newFeatureRow != null) {
            for (Map.Entry<String, Object> fv : uiProperties.entrySet()) {
                newFeatureRow.setValue(fv.getKey(), fv.getValue());
            }
        }
        return newFeatureRow;
    }

    private void showMediaPropertyDialog(View view) {
        OverlayWithIW identifyShape = MapElementsHolder.getIdentifyShape();
        if (identifyShape != null) {
            new MediaPropertyFragment(identifyShape).show(getSupportFragmentManager(), "");
        }
    }

    private Object invokeValue(Class<?> classType, String methodName, String fieldValue) {
        try {
            Method method = classType.getMethod(methodName, String.class);
            return method.invoke(null, fieldValue);
        } catch (Exception e) {
            Log.e(TAG, "invokeValue error: " + e.getMessage());
            return String.valueOf(fieldValue);
        }
    }

    @Override
    protected String showTitleBar() {
        return "Feature属性";
    }

    @Override
    protected boolean needLightTitleBarChild() {
        return true;
    }

    @Override
    protected int getStatusBarColor() {
        return getColor(R.color.common_dark);
    }

    @Override
    protected void onDestroy() {
        if (geoPackage != null) {
            GeoPackageQuick.sink2Database(geoPackage);
        }
        super.onDestroy();
    }
}
