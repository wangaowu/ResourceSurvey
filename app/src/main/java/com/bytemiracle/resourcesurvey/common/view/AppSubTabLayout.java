package com.bytemiracle.resourcesurvey.common.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bytemiracle.base.framework.drawable.RoundRectDrawable;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.R;

import java.util.Iterator;
import java.util.List;

public class AppSubTabLayout extends LinearLayout {
    private int FOCUS_COLOR_VALUE;
    private int NORMAL_COLOR_VALUE;
    private List<AppTabLayout.Pojo> tabDatas;
    private AppTabLayout.OnTabCheckChangedListener onTabCheckedListener;

    private int DPX_5;
    private int DPX_3;

    public AppSubTabLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public AppSubTabLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppSubTabLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.FOCUS_COLOR_VALUE = this.getResources().getColor(R.color.common_text_dark_cyanotic);
        this.NORMAL_COLOR_VALUE = this.getResources().getColor(R.color.app_common_light_gray_2);

        this.DPX_5 = getResources().getDimensionPixelSize(R.dimen.dpx_5);
        this.DPX_3 = getResources().getDimensionPixelSize(R.dimen.dpx_3);
    }

    public AppSubTabLayout initTabs(List<AppTabLayout.Pojo> tabs, AppTabLayout.OnTabCheckChangedListener onTabCheckedListener) {
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

    public List<AppTabLayout.Pojo> getTabDatas() {
        return tabDatas;
    }

    private void addItems() {
        this.removeAllViews();
        if (!ListUtils.isEmpty(this.tabDatas)) {
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.leftMargin = 2 * DPX_5;
            layoutParams.bottomMargin = DPX_5;
            layoutParams.topMargin = DPX_5;
            Iterator var2 = this.tabDatas.iterator();

            while (var2.hasNext()) {
                AppTabLayout.Pojo tab = (AppTabLayout.Pojo) var2.next();
                TextView textView = new TextView(getContext());
                textView.setPadding(4 * DPX_5, DPX_3, 4 * DPX_5, DPX_3);
                this.updateCellState(tab, textView);
                this.addView(textView, layoutParams);
            }
        }
    }

    private void updateItems() {
        if (this.getChildCount() != 0) {
            for (int index = 0; index < this.getChildCount(); ++index) {
                this.updateCellState((AppTabLayout.Pojo) this.tabDatas.get(index), (TextView) this.getChildAt(index));
            }
        }
    }

    private void updateCellState(AppTabLayout.Pojo tab, TextView textView) {
        textView.setText(tab.text);
        textView.setOnClickListener((v) -> {
            if (tab.isChecked()) {
                this.unCheckCell(tab, true);
            } else {
                this.checkCell(tab, true);
            }

        });
        textView.setClickable(!tab.isDisabled());
        if (tab.isChecked()) {
            textView.setTextColor(Color.WHITE);
            textView.setBackground(createFocusRoundRectDrawable());
        } else {
            textView.setTextColor(Color.DKGRAY);
            textView.setBackground(createRoundRectDrawable());
        }
    }

    private Drawable createRoundRectDrawable() {
        return new RoundRectDrawable(Color.LTGRAY, Color.LTGRAY, 2, DPX_5);
    }

    private Drawable createFocusRoundRectDrawable() {
        return new RoundRectDrawable(FOCUS_COLOR_VALUE, FOCUS_COLOR_VALUE, 2, DPX_5);
    }
}
