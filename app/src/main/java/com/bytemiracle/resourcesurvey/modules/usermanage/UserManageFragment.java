package com.bytemiracle.resourcesurvey.modules.usermanage;

import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.BaseFragment;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.resourcesurvey.R;

import butterknife.BindView;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/5/21 11:22
 */
@FragmentTag(name = "用户管理")
public class UserManageFragment extends BaseFragment {
    @BindView(R.id.tv_info)
    TextView tvInfo;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_help_doc;
    }

    @Override
    protected void initViews() {
        tvInfo.setText("用户管理");
    }
}
