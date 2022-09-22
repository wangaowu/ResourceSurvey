package com.bytemiracle.resourcesurvey.common.basecompunent;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bytemiracle.resourcesurvey.R;

/**
 * 类功能：textview按钮代理类
 *
 * @author gwwang
 * @date 2021/5/22 14:27
 */
public class TViewProxy extends AppCompatTextView implements IFunction {

    private STATE state;

    public TViewProxy(@NonNull Context context) {
        this(context, null);
    }

    public TViewProxy(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TViewProxy(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        fixTopDrawable();
        setPadding(0, getResources().getDimensionPixelSize(R.dimen.dpx_2), 0, 0);
        setClickable(true);
        setButtonState(STATE.CLICKABLE);
    }

    public enum STATE {
        ENABLE, DISABLE, CLICKABLE
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

    public STATE getButtonState() {
        return state;
    }

    private void updateButtonState(STATE state) {
        if (state == STATE.CLICKABLE) {
            setClickable(true);
            setBackgroundResource(R.drawable.bg_stroke_black_roundrect_selector);
        } else if (state == STATE.DISABLE) {
            setClickable(false);
            setBackgroundResource(R.drawable.bg_stroke_black_roundrect_selector);
        } else if (state == STATE.ENABLE) {
            setClickable(true);
            setBackgroundResource(R.drawable.bg_stroke_orange_roundrect);
        }
    }

    public void fixTopDrawable() {
        int topDrawableSize = getResources().getDimensionPixelSize(R.dimen.dpx_20);
        Drawable[] compoundDrawables = getCompoundDrawables();
        if (compoundDrawables != null && compoundDrawables.length > 1) {
            Drawable topDrawable = compoundDrawables[1];
            topDrawable.setBounds(0, 0, topDrawableSize, topDrawableSize);
            setCompoundDrawables(null, topDrawable, null, null);
        }
    }

    @Override
    public void onCancel() {

    }
}
