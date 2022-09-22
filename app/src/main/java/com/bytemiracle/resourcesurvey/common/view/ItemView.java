package com.bytemiracle.resourcesurvey.common.view;

import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;

import com.bytemiracle.base.R.color;
import com.bytemiracle.base.R.id;
import com.bytemiracle.base.R.layout;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.FieldDict;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;
import com.xuexiang.xui.widget.picker.widget.TimePickerView;
import com.xuexiang.xui.widget.picker.widget.builder.TimePickerBuilder;
import com.xuexiang.xui.widget.picker.widget.configure.TimePickerType;
import com.xuexiang.xutil.common.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ItemView {
    public static View addNumberItem(LinearLayout llContainer, ItemViewData itemViewData) {
        View itemView = View.inflate(llContainer.getContext(), R.layout.item_edit_text, (ViewGroup) null);
        llContainer.addView(itemView, new LinearLayout.LayoutParams(-1, -2));
        TextView tvFlag = (TextView) itemView.findViewById(id.tv_flag);
        EditText etContent = (EditText) itemView.findViewById(id.et_content);
        int textColorResId = false ? color.app_common_content_text_dark_333_color : color.app_common_content_text_light_gray_color;
        tvFlag.setTextColor(llContainer.getContext().getColor(textColorResId));
        tvFlag.setText(itemViewData.name);
        if (itemViewData.numberType.equals("integer")) {
            etContent.setTag(itemViewData.intVal);
            etContent.setText(String.valueOf(itemViewData.intVal));
            etContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        } else if (itemViewData.numberType.equals("double")) {
            etContent.setTag(itemViewData.doubleVal);
            etContent.setText(String.valueOf(itemViewData.doubleVal));
            etContent.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
        return itemView;
    }

    public static View addRadioItem(LinearLayout llContainer, ItemViewData itemViewData) {
        View itemView = View.inflate(llContainer.getContext(), R.layout.item_radio, (ViewGroup) null);
        llContainer.addView(itemView, new LinearLayout.LayoutParams(-1, -2));
        TextView tvFlag = (TextView) itemView.findViewById(id.tv_flag);
        RadioGroup radioGroup = (RadioGroup) itemView.findViewById(id.radio_group);
        int textColorResId = false ? color.app_common_content_text_dark_333_color : color.app_common_content_text_light_gray_color;
        tvFlag.setTextColor(llContainer.getContext().getColor(textColorResId));
        tvFlag.setText(itemViewData.name);
        TextView tvStar = (TextView) itemView.findViewById(id.view_star);
        tvStar.setVisibility(View.GONE);
        radioGroup.setTag(itemViewData.booleanVal);
        radioGroup.check(itemViewData.booleanVal ? R.id.yes : R.id.no);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> radioGroup.setTag(checkedId == R.id.yes ? true : false));
        return itemView;
    }

    public static View addDateItem(LinearLayout llContainer, ItemViewData itemViewData) {
        View itemView = View.inflate(llContainer.getContext(), layout.item_flag_select_date_layout, (ViewGroup) null);
        llContainer.addView(itemView, new LinearLayout.LayoutParams(-1, -2));
        TextView tvFlag = (TextView) itemView.findViewById(id.tv_flag);
        TextView tvContent = (TextView) itemView.findViewById(id.tv_content);
        tvContent.setVisibility(View.VISIBLE);
        int textColorResId = false ? color.app_common_content_text_dark_333_color : color.app_common_content_text_light_gray_color;
        tvFlag.setTextColor(llContainer.getContext().getColor(textColorResId));
        tvFlag.setText(itemViewData.name);
        tvContent.setTag(itemViewData.dateVal);
        tvContent.setText(itemViewData.dateVal != null ? AppTime.formatDate(itemViewData.dateVal) : "请点击选择");
        tvContent.setOnClickListener((v) -> {
            int dialogMainColor = llContainer.getContext().getResources().getColor(R.color.common_dark);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            TimePickerView mTimePickerDialog = (new TimePickerBuilder(itemView.getContext(), (date, v1) -> {
                tvContent.setTag(date);
                tvContent.setText(AppTime.formatDate(date));
            })).setType(TimePickerType.DEFAULT).setTitleText("日期").setTitleBgColor(dialogMainColor).setTitleColor(-1).setCancelColor(dialogMainColor).setSubmitColor(dialogMainColor).isDialog(true).setOutSideCancelable(false).setDate(calendar).build();
            mTimePickerDialog.show();
        });
        return itemView;
    }

    public static View addSingleCheckItem(LinearLayout llContainer, ItemViewData itemViewData, FragmentManager fragmentManager) {
        FieldDict.Pair[] pairs = JsonParser.fromJson(itemViewData.getCheckPool(), FieldDict.Pair[].class);

        View itemView = View.inflate(llContainer.getContext(), R.layout.item_select, (ViewGroup) null);
        llContainer.addView(itemView, new LinearLayout.LayoutParams(-1, -2));
        TextView tvFlag = (TextView) itemView.findViewById(R.id.tv_flag);
        TextView tvContent = (TextView) itemView.findViewById(R.id.tv_content);
        int textColorResId = false ? color.app_common_content_text_dark_333_color : color.app_common_content_text_light_gray_color;
        tvFlag.setTextColor(llContainer.getContext().getColor(textColorResId));
        tvFlag.setText(itemViewData.aliasName);
        tvContent.setTag(itemViewData.stringVal);
        tvContent.setText(!TextUtils.isEmpty(itemViewData.stringVal) ? FieldDict.getValueArrayString(pairs, itemViewData.stringVal) : "请点击选择");
        tvContent.setOnClickListener((v) ->
                new MaterialDialog.Builder(llContainer.getContext())
                        .title(itemViewData.name)
                        .items(FieldDict.getValueArrays(pairs))
                        .itemsCallbackSingleChoice(-1, (materialDialog, view, i, charSequence) -> {
                            String text = charSequence.toString();
                            tvContent.setText(text);
                            tvContent.setTag(FieldDict.getKeyArrayString(pairs, Arrays.asList(text)));
                            return true;
                        })
                        .show());
        return itemView;
    }

    public static View addMultiCheckItem(LinearLayout llContainer, ItemViewData itemViewData, FragmentManager fragmentManager) {
        FieldDict.Pair[] pairs = JsonParser.fromJson(itemViewData.getCheckPool(), FieldDict.Pair[].class);

        View itemView = View.inflate(llContainer.getContext(), R.layout.item_select, (ViewGroup) null);
        llContainer.addView(itemView, new LinearLayout.LayoutParams(-1, -2));
        TextView tvFlag = (TextView) itemView.findViewById(R.id.tv_flag);
        TextView tvContent = (TextView) itemView.findViewById(R.id.tv_content);
        int textColorResId = false ? color.app_common_content_text_dark_333_color : color.app_common_content_text_light_gray_color;
        tvFlag.setTextColor(llContainer.getContext().getColor(textColorResId));
        tvFlag.setText(itemViewData.aliasName);
        tvContent.setTag(itemViewData.stringVal);
        tvContent.setText(!TextUtils.isEmpty(itemViewData.stringVal) ? FieldDict.getValueArrayString(pairs, itemViewData.stringVal) : "请点击选择");
        tvContent.setOnClickListener((v) -> {
            new MaterialDialog.Builder(llContainer.getContext())
                    .title(itemViewData.name)
                    .items(FieldDict.getValueArrays(pairs))
                    .itemsCallbackMultiChoice(new Integer[]{-1}, (materialDialog, integers, charSequences) -> {
                        tvContent.setText(Arrays.stream(charSequences).collect(Collectors.joining(",")));
                        tvContent.setTag(FieldDict.getKeyArrayString(pairs, Arrays.asList(charSequences)));
                        return true;
                    })
                    .alwaysCallMultiChoiceCallback()
                    .show();
        });
        return itemView;
    }

    // param str : "id1,id2;code1,code2;name1,name2"
    // return : "code1 name1;code2 name2"
    public static String showMultiVal(String str) {
        if (StringUtils.isEmpty(str)) {
            return "请点击选择";
        }
        String[] arr = str.split(";");
        String[] codeArr = arr[1].split(",");
        String[] nameArr = arr[2].split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeArr.length; i++) {
            sb.append(";" + codeArr[i] + " " + nameArr[i]);
        }
        return sb.substring(1);
    }

    // param str : "id1,id2;code1,code2;name1,name2"
    // return ["id1", "id2"]
    public static List<String> selectIds(String str) {
        List<String> list = new ArrayList<>();
        if (!StringUtils.isEmpty(str)) {
            list.addAll(Arrays.asList(str.split(";")[0].split(",")));
        }
        return list;
    }
}
