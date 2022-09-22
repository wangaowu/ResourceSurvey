package com.bytemiracle.resourcesurvey.common.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bytemiracle.base.framework.drawable.RoundRectDrawable;
import com.bytemiracle.base.framework.fragment.BaseFragment;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.image.BitmapWrapper;
import com.bytemiracle.base.framework.view.BaseCheckPojo;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;

import java.util.Iterator;
import java.util.List;

public class AppTabLayout extends LinearLayout {
    private int FOCUS_COLOR_VALUE;
    private int NORMAL_COLOR_VALUE;
    private List<AppTabLayout.Pojo> tabDatas;
    private AppTabLayout.OnTabCheckChangedListener onTabCheckedListener;

    public AppTabLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public AppTabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.FOCUS_COLOR_VALUE = this.getResources().getColor(R.color.common_text_dark_cyanotic);
        this.NORMAL_COLOR_VALUE = this.getResources().getColor(R.color.app_common_dark_cyanotic);
    }

    public AppTabLayout initTabs(List<AppTabLayout.Pojo> tabs, AppTabLayout.OnTabCheckChangedListener onTabCheckedListener) {
        this.tabDatas = tabs;
        this.onTabCheckedListener = onTabCheckedListener;
        this.addItems();
        return this;
    }

    public void checkFirst() {
        this.checkCell((AppTabLayout.Pojo) this.tabDatas.get(0), false);
    }

    public void checkCell(int index) {
        this.checkCell((AppTabLayout.Pojo) this.tabDatas.get(index), false);
    }

    public void checkCell(AppTabLayout.Pojo tab, boolean isClick) {
        tab.setChecked(true);
        this.onTabCheckedListener.onTabChecked(tab, isClick);
        this.updateItems();
    }

    public void unCheckCell(AppTabLayout.Pojo tab, boolean isClick) {
        tab.setChecked(false);
        this.onTabCheckedListener.onTabChecked(tab, isClick);
        this.updateItems();
    }

    private void addItems() {
        this.removeAllViews();
        if (!ListUtils.isEmpty(this.tabDatas)) {
            LayoutParams layoutParams = new LayoutParams(0, -1);
            layoutParams.weight = 1.0F;
            Iterator var2 = this.tabDatas.iterator();

            while (var2.hasNext()) {
                AppTabLayout.Pojo tab = (AppTabLayout.Pojo) var2.next();
                View cell = View.inflate(this.getContext(), R.layout.layout_app_tab_cell, (ViewGroup) null);
                this.updateCellState(tab, cell);
                this.addView(cell, layoutParams);
            }
        }

    }

    private void updateItems() {
        if (this.getChildCount() != 0) {
            for (int index = 0; index < this.getChildCount(); ++index) {
                this.updateCellState((AppTabLayout.Pojo) this.tabDatas.get(index), this.getChildAt(index));
            }
        }
    }

    private void updateCellState(AppTabLayout.Pojo tab, View cell) {
        TextView textView = (TextView) cell.findViewById(R.id.tv_content);
        ImageView iv = (ImageView) cell.findViewById(R.id.iv);
        textView.setText(tab.text);
        iv.setImageResource(tab.bottomDrawableResId);
        cell.setOnClickListener((v) -> {
            if (tab.isChecked()) {
                this.unCheckCell(tab, true);
            } else {
                this.checkCell(tab, true);
            }

        });
        cell.setClickable(!tab.isDisabled);
        if (tab.isChecked()) {
            textView.setTextColor(Color.WHITE);
            applyImage(iv, tab.bottomDrawableResId, Color.WHITE);
            cell.setBackground(new ColorDrawable(this.FOCUS_COLOR_VALUE));
        } else {
            if (tab.isDisabled) {
                textView.setTextColor(Color.WHITE);
                applyImage(iv, tab.bottomDrawableResId, this.NORMAL_COLOR_VALUE);
            } else {
                textView.setTextColor(Color.WHITE);
                applyImage(iv, tab.bottomDrawableResId, this.FOCUS_COLOR_VALUE);
            }
            cell.setBackground(createRoundRectDrawable());
        }
    }

    private void applyImage(ImageView iv, int drawableResId, int colorValue) {
        if (drawableResId != 0) {
            BitmapWrapper.quickApply(iv, drawableResId, colorValue);
        }
    }

    private Drawable createRoundRectDrawable() {
        return new RoundRectDrawable(Color.WHITE, NORMAL_COLOR_VALUE, 1, 0);
    }

    public static class Pojo extends BaseCheckPojo {
        public int bottomDrawableResId;
        public String text;
        public Class<? extends BaseFragment> fragmentClazz;
        public BaseDialogFragment dialogFragment;
        private boolean isDisabled;

        public Pojo(int bottomDrawableResId, String text, boolean isDisabled, boolean isChecked) {
            this(bottomDrawableResId, text, null, isDisabled, isChecked);
        }

        public Pojo(String text, boolean isChecked, BaseDialogFragment dialogFragment) {
            this.text = text;
            this.setChecked(isChecked);
            this.dialogFragment = dialogFragment;
        }

        public Pojo(int bottomDrawableResId, String text, Class<? extends BaseFragment> fragmentClazz, boolean isDisabled, boolean isChecked) {
            this.bottomDrawableResId = bottomDrawableResId;
            this.text = text;
            this.fragmentClazz = fragmentClazz;
            this.isDisabled = isDisabled;
            this.setChecked(isChecked);
        }

        public boolean isDisabled() {
            return this.isDisabled;
        }

        public void setDisabled(boolean disabled) {
            this.isDisabled = disabled;
        }
    }

    public interface OnTabCheckChangedListener {
        void onTabChecked(AppTabLayout.Pojo var1, boolean var2);
    }
}
