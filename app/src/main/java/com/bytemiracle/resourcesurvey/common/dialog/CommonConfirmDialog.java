package com.bytemiracle.resourcesurvey.common.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.resourcesurvey.R;

/**
 * 类功能：通用的确认弹窗
 *
 * @author gwwang
 * @date 2021/3/18 14:13
 */
public class CommonConfirmDialog extends DialogFragment {

    private String title;
    private String hint;
    private CommonAsync2Listener<CommonConfirmDialog> clickButtonListener;

    public CommonConfirmDialog(String title, String hint, CommonAsync2Listener<CommonConfirmDialog> clickButtonListener) {
        this.title = title;
        this.hint = hint;
        this.clickButtonListener = clickButtonListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getContext(), R.layout.dialog_common_confirm, null);
        initViews(contentView);
        return contentView;
    }

    private void initViews(View view) {
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            clickButtonListener.doSomething1(CommonConfirmDialog.this);
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            clickButtonListener.doSomething2(CommonConfirmDialog.this);
        });
        view.findViewById(R.id.fl_close).setOnClickListener(v -> {
            dismiss();
        });
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvHint = view.findViewById(R.id.tv_hint);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (!TextUtils.isEmpty(hint)) {
            tvHint.setText(hint);
        }
    }
}
