package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.view.View;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;

/**
 * 类功能：屏幕图层
 *
 * @author gwwang
 * @date 2021/5/22 15:04
 */
@FragmentTag(name = "屏幕图层")
public class ScreenLayersFragment extends BaseDialogFragment {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_manage_project;
    }

    @Override
    protected void initViews(View view) {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("新建");
        appTitleController.getRightButton().setOnClickListener(v -> {

        });
    }


}
