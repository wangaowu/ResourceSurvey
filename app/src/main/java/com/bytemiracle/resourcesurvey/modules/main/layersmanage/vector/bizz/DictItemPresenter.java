package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.FieldDict;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.PreviewImportFieldFragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/11 13:43
 */
public class DictItemPresenter {

    private LinearLayout llFieldContainer;
    private WeakReference<Context> context;

    public DictItemPresenter(Context context, LinearLayout llFieldContainer) {
        this.context = new WeakReference(context);
        this.llFieldContainer = llFieldContainer;
    }

    /**
     * 更新字段的UI条目
     *
     * @param overlayFieldDict
     */
    public void updateFieldsUi(List<DBFieldDict> overlayFieldDict) {
        llFieldContainer.removeAllViews();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        for (DBFieldDict fieldDict : overlayFieldDict) {
            View itemField = View.inflate(context.get(), R.layout.item_layer_field_dict_config, null);
            TextView tvFieldName = itemField.findViewById(R.id.tv_field_name);
            LinearLayout llDictContainer = itemField.findViewById(R.id.ll_dict_container);
            View dictGroup = itemField.findViewById(R.id.dict_group);
            RadioGroup rgType = itemField.findViewById(R.id.rg_type);
            //checkbox事件
            rgType.setOnCheckedChangeListener((radioGroup, checkedId) -> {
                int checkType;
                if (checkedId == R.id.rb_type_input) {
                    checkType = DBFieldDict.TYPE_INPUT_CHECK;
                    dictGroup.setBackgroundColor(Color.TRANSPARENT);
                    llDictContainer.setVisibility(View.GONE);
                    llDictContainer.removeAllViews();
                } else {
                    if (checkedId == R.id.rb_type_single) {
                        checkType = DBFieldDict.TYPE_SINGLE_CHECK;
                    } else {
                        checkType = DBFieldDict.TYPE_MULTI_CHECK;
                    }
                    dictGroup.setBackgroundResource(R.drawable.rect_dot_line_orange);
                    llDictContainer.setVisibility(View.VISIBLE);
                    updateDictUI(llDictContainer, fieldDict.getDictValues());
                }
                fieldDict.setCheckType(checkType);
            });
            //导入按钮
            itemField.findViewById(R.id.ib_import).setOnClickListener(v -> {
                new PreviewImportFieldFragment(fieldDict.getFieldName(), fieldDictResult -> {
                    fieldDict.setDictValues(fieldDictResult.getDictValues());
                    fieldDict.setCheckType(fieldDictResult.getCheckType());
                    fieldDict.setFieldValuePool(JsonParser.toJson(fieldDictResult.getDictValues()));
                    updateDictUI(llDictContainer, fieldDictResult.getDictValues());
                }).show(((BaseActivity) context.get()).getSupportFragmentManager(), "");
            });
            //应用按钮
            itemField.findViewById(R.id.ib_apply).setOnClickListener(v -> {
                //追加最后一条
                int childCount = llDictContainer.getChildCount();
                if (childCount != 0) {
                    View lastItem = llDictContainer.getChildAt(childCount - 1);
                    EditText etValueContent = lastItem.findViewById(R.id.et_value_content);
                    EditText etValueKey = lastItem.findViewById(R.id.et_value_key);
                    String lastContent = etValueContent.getText().toString();
                    String lastKey = etValueKey.getText().toString();
                    if (!TextUtils.isEmpty(lastContent) && !TextUtils.isEmpty(lastKey)) {
                        fieldDict.getDictValues().add(new FieldDict.Pair(lastKey, lastContent));
                    }
                }
                //将list--->json之后，更新到数据库内
                List<FieldDict.Pair> dictValues = fieldDict.getDictValues();
                List<FieldDict.Pair> filterEmptyList = dictValues.stream().filter(d -> d != null).collect(Collectors.toList());
                fieldDict.setDictValues(filterEmptyList);
                FieldDict.updateDictValues(fieldDict);
                if (FieldDictProvider.insertOrReplaceFieldDict(fieldDict)) {
                    XToastUtils.info("应用成功!");
                }
            });

            switch (fieldDict.getCheckType()) {
                case DBFieldDict.TYPE_INPUT_CHECK:
                    rgType.check(R.id.rb_type_input);
                    break;
                case DBFieldDict.TYPE_SINGLE_CHECK:
                    rgType.check(R.id.rb_type_single);
                    break;
                case DBFieldDict.TYPE_MULTI_CHECK:
                    rgType.check(R.id.rb_type_multi);
                    break;
            }
            tvFieldName.setText(fieldDict.getFieldName());
            llFieldContainer.addView(itemField, layoutParams);
        }
    }

    /**
     * 更新字典的UI条目
     *
     * @param llDictContainer
     * @param dictValues
     */
    private void updateDictUI(LinearLayout llDictContainer, List<FieldDict.Pair> dictValues) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        llDictContainer.removeAllViews();
        if (!ListUtils.isEmpty(dictValues)) {
            for (int i = 0; i < dictValues.size(); i++) {
                View item = getEditableDictItem(llDictContainer, dictValues, i);
                llDictContainer.addView(item, layoutParams);
            }
        }
        View addItem = getEditableDictItem(llDictContainer, dictValues, -1);
        llDictContainer.addView(addItem, layoutParams);
    }

    private View getEditableDictItem(LinearLayout llDictContainer, List<FieldDict.Pair> dictValues, int index) {
        View item = View.inflate(context.get(), R.layout.item_editable_field_dict_value, null);
        EditText etValueContent = item.findViewById(R.id.et_value_content);
        EditText etValueKey = item.findViewById(R.id.et_value_key);
        ImageButton ibOperate = item.findViewById(R.id.ib_operate);
        if (index != -1) {
            //编辑选项的vm数据绑定
            etValueContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    dictValues.get(index).value = s.toString();
                }
            });
            etValueContent.setText(dictValues.get(index).value);
            etValueKey.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    dictValues.get(index).key = s.toString();
                }
            });
            etValueKey.setText(dictValues.get(index).key);
            //删除
            ibOperate.setImageResource(R.drawable.sharp_remove_black_36);
            ibOperate.setOnClickListener(v -> {
                dictValues.remove(index);
                updateDictUI(llDictContainer, dictValues);
            });
        } else {
            //添加
            ibOperate.setImageResource(R.drawable.sharp_add_black_36);
            ibOperate.setOnClickListener(v -> {
                String valueContent = etValueContent.getText().toString().trim();
                String valueKey = etValueKey.getText().toString().trim();
                if (!TextUtils.isEmpty(valueKey) && !TextUtils.isEmpty(valueContent)) {
                    dictValues.add(new FieldDict.Pair(valueKey, valueContent));
                    updateDictUI(llDictContainer, dictValues);
                } else {
                    XToastUtils.info("请检查字典无空项!");
                }
            });
        }
        return item;
    }
}
