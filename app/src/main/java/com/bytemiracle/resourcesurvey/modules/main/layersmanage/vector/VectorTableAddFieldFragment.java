package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.dynamicitem.DynamicItemPresenter;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemController;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemData;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import org.osmdroid.customImpl.geopackage.FieldDefn;

import butterknife.BindView;
import mil.nga.geopackage.db.GeoPackageDataType;

/**
 * 类功能：添加shp表字段
 *
 * @author gwwang
 * @date 2021/5/26 10:50
 */
@FragmentTag(name = "添加字段属性")
public class VectorTableAddFieldFragment extends BaseDialogFragment {
    private static final String TAG = "VectorTableAddFieldFragment";

    @BindView(R.id.ll_container)
    LinearLayout llContainer;
    private ItemController fieldNameItem;
    private MaterialSpinner spinnerFieldType;

    public interface GetFieldsListener {
        void onFieldCreate(FieldDefn fieldDefn);
    }

    private GetFieldsListener getFieldsListener;

    /**
     * 构造方法
     *
     * @param getFieldsListener 获取字段结果的监听器
     */
    public VectorTableAddFieldFragment(GetFieldsListener getFieldsListener) {
        this.getFieldsListener = getFieldsListener;
    }

    @Override
    protected void initViews(View view) {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("确定");
        appTitleController.getRightButton().setOnClickListener(v -> createField());

        //内容
        DynamicItemPresenter dynamicItemPresenter = new DynamicItemPresenter(llContainer);
        fieldNameItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "字段名称").editHint("输入字段名称").content(""));
        View inflate = View.inflate(getContext(), R.layout.item_flag_spinner_layout, null);
        spinnerFieldType = inflate.findViewById(R.id.spinner_field_type);
        ((TextView) inflate.findViewById(R.id.tv_flag)).setText("字段类型");
        dynamicItemPresenter.addItem(inflate);

        String[] geoPackageDataDefineTypes = FieldDefn.fieldMap.keySet().toArray(new String[0]);
        spinnerFieldType.setItems(geoPackageDataDefineTypes);
        spinnerFieldType.setTag(geoPackageDataDefineTypes[0]);
        spinnerFieldType.setOnItemSelectedListener((view1, position, id, item) -> {
            spinnerFieldType.setTag(geoPackageDataDefineTypes[position]);
        });
        spinnerFieldType.setSelectedIndex(0);
    }

    /**
     * 新增字段
     *
     * @return
     */
    private void createField() {
        String fieldName = fieldNameItem.findEditText().getText().toString().trim();
        String fieldTypeName = FieldDefn.fieldMap.get(spinnerFieldType.getTag());
        if (TextUtils.isEmpty(fieldName)) {
            XToastUtils.info("字段名称不能为空!");
            return;
        }
        if (fieldTypeName == null) {
            XToastUtils.info("字段类型不能为空!");
            return;
        }
        FieldDefn fieldDefn;
        GeoPackageDataType fieldType = FieldDefn.matchGeoPackageDataType(fieldTypeName);
        fieldDefn = FieldDefn.create(fieldName, fieldType, null);
        getFieldsListener.onFieldCreate(fieldDefn);
        dismiss();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_vector_add_table_property;
    }

    @Override
    protected float getHeightRatio() {
        return .3f;
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }
}
