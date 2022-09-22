package com.bytemiracle.resourcesurvey.common.basecompunent;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.ImageView;

import com.bytemiracle.base.framework.utils.XToastUtils;

/**
 * 类功能：按钮代理类
 *
 * @author gwwang
 * @date 2021/5/22 10:23
 */
public class ButtonProxy implements IFunction {

    private STATE state;
    private View.OnClickListener onClickListener;

    @Override
    public void onCancel() {

    }

    public enum STATE {
        GONE, VISIBLE, ENABLE, DISABLE, CLICKABLE
    }

    private ImageView view;
    private int[] resIDs;

    /**
     * 构建代理类
     *
     * @param view   控件引用
     * @param resIDs 背景状态使用的资源id[3]   0：正常  1：点击  2：禁用
     */
    public ButtonProxy(ImageView view, int... resIDs) {
        this.view = view;
        this.resIDs = resIDs;
        initButton();
    }

    private void initButton() {
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        setButtonState(STATE.CLICKABLE);
        view.setOnClickListener(v -> {
            if (getButtonState() == STATE.DISABLE) {
                XToastUtils.info("功能禁用!");
                return;
            }
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        });
    }

    /**
     * 设置点击事件
     *
     * @param onClickListener
     */
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * 设置按钮状态
     *
     * @param state
     */
    public void setButtonState(STATE state) {
        this.state = state;
        updateButtonState(state);
    }

    /**
     * 获取按钮状态
     *
     * @return
     */
    public STATE getButtonState() {
        return state;
    }

    private void updateButtonState(STATE state) {
        if (state == STATE.GONE) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        if (state == STATE.CLICKABLE) {
            view.setClickable(true);
            view.setImageDrawable(buildStateListDrawable());
        } else if (state == STATE.DISABLE) {
            view.setClickable(false);
            view.setImageDrawable(getDrawable(resIDs[DISABLE]));
        } else if (state == STATE.ENABLE) {
            view.setClickable(true);
            view.setImageDrawable(getDrawable(resIDs[PRESSED]));
        }
    }

    private StateListDrawable buildStateListDrawable() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, getDrawable(resIDs[PRESSED]));
        stateListDrawable.addState(new int[]{}, getDrawable(resIDs[NORM]));
        return stateListDrawable;
    }

    private Drawable getDrawable(int resId) {
        return view.getContext().getResources().getDrawable(resId);
    }
}
