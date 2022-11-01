package com.bytemiracle.resourcesurvey.modules.summaryquery;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * 类功能：查询条件构建界面
 *
 * @author gwwang
 * @date 2022/6/30 9:07
 */
@FragmentTag(name = "条件编辑器")
public class ConditionsDialog extends BaseDialogFragment {
    private static final String TAG = "ConditionsDialog";
    //空格常量
    private static final String SPACE = " ";
    private static final String CHAR_APOSTROPHE = "\'";
    private static final String CHAR_PERCENT_SIGN = "%";
    //操作符
    private static final String[] OPERATORS = new String[]{
            "=", "!=",
            "<", ">",
            ">=", "<=",
            "OR", "AND",
            "IN", "NOT IN",
            "LIKE", "NOT",
            CHAR_APOSTROPHE, CHAR_PERCENT_SIGN,
    };

    @BindView(R.id.et_conditions)
    EditText etConditions;
    @BindView(R.id.rv_fields)
    SwipeRecyclerView rvFields;
    @BindView(R.id.gv_operators)
    GridView gvOperators;
    @BindView(R.id.btn_delete)
    Button btnDelete;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private List<String> columnNames;
    private CommonAsyncListener<String> conditionResultListener;

    /**
     * 设置初始化数据
     *
     * @param columnNames             列名
     * @param conditionResultListener 选中的条件结果回调
     * @return
     */
    public ConditionsDialog setDialogDataListener(List<String> columnNames, CommonAsyncListener<String> conditionResultListener) {
        this.columnNames = columnNames;
        this.conditionResultListener = conditionResultListener;
        return this;
    }

    @Override
    protected void initViews(View view) {
        clearCondition();
        initColumns(clickedColumn -> appendCondition(clickedColumn));
        gvOperators.post(() -> {
            int height = gvOperators.getHeight();
            initOperators(height, clickedOperator -> appendCondition(clickedOperator));
        });

        btnDelete.setOnClickListener(v -> executeAutoDelete());
        btnConfirm.setOnClickListener(v -> {
            conditionResultListener.doSomething(etConditions.getText().toString());
            dismiss();
        });
    }

    private void initColumns(CommonAsyncListener<String> clickItemListener) {
        rvFields.setLayoutManager(new FlexboxLayoutManager(getContext()));

        QuickList.instance().adapter(rvFields, R.layout.item_calc_text, columnNames,
                new QuickListListener<String>() {
                    @Override
                    public void onBindItem(QuickAdapter adapter, SmartViewHolder holder, String columnName) {
                        TextView tvContent = holder.itemView.findViewById(R.id.tv_content);
                        tvContent.setBackground(null);
                        tvContent.getPaint().setUnderlineText(true);
                        tvContent.setTextColor(getResources().getColor(R.color.app_common_dark_cyanotic));
                        tvContent.setText(columnName);

                        holder.itemView.setOnClickListener(v -> clickItemListener.doSomething(columnName));
                    }
                });
    }

    private void initOperators(int parentHeight, CommonAsyncListener<String> clickItemListener) {
        int DP10 = getContext().getResources().getDimensionPixelSize(R.dimen.dpx_10);
        float numColumns = gvOperators.getNumColumns();
        double numRows = Math.ceil(OPERATORS.length / numColumns);
        int itemShouldHeight = (int) ((parentHeight - (numRows - 1) * DP10) / numRows);
        gvOperators.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return OPERATORS.length;
            }

            @Override
            public Object getItem(int position) {
                return OPERATORS[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                String operator = OPERATORS[position];
                TextView textView = (TextView) View.inflate(getContext(), R.layout.item_calc_text, null);
                textView.setText(operator);
                textView.setOnClickListener(v -> clickItemListener.doSomething(operator));
                textView.setMinHeight(itemShouldHeight);
                return textView;
            }
        });
    }

    private void executeAutoDelete() {
        String content = etConditions.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            //先删除空格
            if (content.endsWith(SPACE)) {
                String substring = content.substring(0, content.length() - 1);
                etConditions.setText(substring);
                moveCursorToLast();
                return;
            }
            //是否以操作符结尾
            String endWithOperator = endWithIn(content, Arrays.asList(OPERATORS));
            if (!TextUtils.isEmpty(endWithOperator)) {
                String substring = content.substring(0, content.length() - endWithOperator.length());
                etConditions.setText(substring);
                moveCursorToLast();
                return;
            }
            //是否以列名称结尾
            String endWithColumn = endWithIn(content, columnNames);
            if (!TextUtils.isEmpty(endWithColumn)) {
                String substring = content.substring(0, content.length() - endWithColumn.length());
                etConditions.setText(substring);
                moveCursorToLast();
                return;
            }
            //挨个删除
            String substring = content.substring(0, content.length() - 1);
            etConditions.setText(substring);
            moveCursorToLast();
        }
    }

    private String endWithIn(String content, List<String> endPrefix) {
        for (String prefix : endPrefix) {
            if (content.endsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private void clearCondition() {
        etConditions.setText("");
    }

    private void appendCondition(String split) {
        String content = etConditions.getText().toString();
        StringBuffer append = new StringBuffer(content).append(split);
        if (CHAR_PERCENT_SIGN.equals(split) || CHAR_APOSTROPHE.equals(split)) {
            // % 和 ‘ 不增加空格
        } else {
            append.append(SPACE);
        }
        etConditions.setText(append);
        moveCursorToLast();
    }

    private void moveCursorToLast() {
        String content = etConditions.getText().toString();
        etConditions.requestFocus();
        etConditions.setSelection(content.length());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_conditions;
    }

    @Override
    protected float getWidthRatio() {
        return .5f;
    }
}
