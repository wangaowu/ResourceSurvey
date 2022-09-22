package com.bytemiracle.resourcesurvey.common.dialog;

import android.view.View;
import android.widget.EditText;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.xuexiang.xutil.common.StringUtils;

import butterknife.BindView;

/**
 * 类功能：通用的确认弹窗
 *
 * @author gwwang
 * @date 2021/3/18 14:13
 */
@FragmentTag(name = "输入绘制文字")
public class CommonGetTextDialog extends BaseDialogFragment {
    @BindView(R.id.et_text)
    EditText editText;
    @BindView(R.id.btn_confirm)
    View btnConfirm;
    @BindView(R.id.btn_cancel)
    View btnCancel;

    private CommonAsyncListener<String> getTextListener;

    public CommonGetTextDialog(CommonAsyncListener<String> getTextListener) {
        this.getTextListener = getTextListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_get_input_text;
    }

    @Override
    protected float getHeightRatio() {
        return .2f;
    }

    @Override
    protected float getWidthRatio() {
        return .5f;
    }

    @Override
    protected void initViews(View view) {
        btnConfirm.setOnClickListener(v -> {
            String inputText = editText.getText().toString();
            if (StringUtils.isEmpty(inputText)) {
                XToastUtils.info("不能是空文字!");
                return;
            }
            getTextListener.doSomething(inputText);
            dismiss();
        });
        btnCancel.setOnClickListener(v -> dismiss());
    }
}
