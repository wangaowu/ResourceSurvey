package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync4Listener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 类功能：滚动详情dialog
 *
 * @author gwwang
 * @date 2021/7/8 9:35
 */
@FragmentTag(name = "详情")
public class DetailScrollInfoDialog extends BaseDialogFragment {
    private static final String TAG = "DetailScrollInfoDialog";

    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    private QuickAdapter adapter;
    private List<String> scrollInfos = new ArrayList<>();
    private CommonAsync4Listener<String> getScrollInfoListener;

    public DetailScrollInfoDialog() {
    }

    @Override
    protected void initViews(View view) {
        btnConfirm.setOnClickListener(v -> dismiss());
        showButton(false);
        adapter = QuickList.instance().adapter(rvContent, android.R.layout.simple_list_item_1, scrollInfos, new QuickListListener<String>() {
            @Override
            public void onBindItem(QuickAdapter quickAdapter, SmartViewHolder h, String singleInfo) {
                h.text(android.R.id.text1, singleInfo);
            }
        });
    }

    public void appendInfo(String lineContent) {
        if (adapter != null) {
            adapter.getListData().add(lineContent);
            adapter.notifyDataSetChanged();
            rvContent.scrollToPosition(adapter.getListData().size() - 1);
        }
        showButton(true);
    }

    private void showButton(boolean show) {
        btnConfirm.setVisibility(!show ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_detail_scroll;
    }

    @Override
    protected float getHeightRatio() {
        return .4f;
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }
}
