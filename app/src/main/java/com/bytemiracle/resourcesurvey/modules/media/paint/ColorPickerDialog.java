package com.bytemiracle.resourcesurvey.modules.media.paint;

import android.view.View;
import android.widget.LinearLayout;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;

import butterknife.BindView;

/**
 * 选择颜色弹窗
 */
@FragmentTag(name = "颜色")
public class ColorPickerDialog extends BaseDialogFragment {

    @BindView(R.id.ll_container)
    LinearLayout llContainer;

    private ColorPickerView colorPickerView;
    private ColorPickerView.OnColorChangedListener mListener;
    private int mInitialColor;

    /**
     * 构造方法
     *
     * @param mInitialColor               初始化颜色
     * @param paramOnColorChangedListener 颜色改变监听
     */
    public ColorPickerDialog(int mInitialColor, ColorPickerView.OnColorChangedListener paramOnColorChangedListener) {
        this.mInitialColor = mInitialColor;
        this.mListener = paramOnColorChangedListener;
    }

    @Override
    protected void initViews(View view) {
        colorPickerView = new ColorPickerView(getContext());
        llContainer.addView(colorPickerView);
        colorPickerView.setColorCallback(mInitialColor, pickedColor -> {
            mListener.colorChanged(pickedColor);
            dismiss();
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_base_container;
    }

    @Override
    protected float getWidthRatio() {
        return .5f;
    }
}
