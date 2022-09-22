package com.bytemiracle.resourcesurvey.modules.help;

import android.widget.TextView;

import androidx.core.text.HtmlCompat;

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
@FragmentTag(name = "帮助")
public class HelpDocFragment extends BaseFragment {

    @BindView(R.id.tv_info)
    TextView tvInfo;

    @Override
    protected void initViews() {
        String helpDocText = getString(R.string.help_doc_text);
        tvInfo.setText(HtmlCompat.fromHtml(helpDocText, 0));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_help_doc;
    }
}
