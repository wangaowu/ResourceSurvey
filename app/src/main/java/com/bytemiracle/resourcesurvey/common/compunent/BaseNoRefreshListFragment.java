package com.bytemiracle.resourcesurvey.common.compunent;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytemiracle.base.framework.fragment.list.BaseListFragment;

/**
 * 类功能：无刷新的基类listFragment
 *
 * @author gwwang
 * @date 2021/5/24 16:13
 */
public abstract class BaseNoRefreshListFragment<V> extends BaseListFragment<V> {

    @Override
    protected void doRefresh(SwipeRefreshLayout swipeRefreshLayout) {
        refreshComplete();
    }

    @Override
    protected void initViews() {
        super.initViews();
        disableRefresh();
    }

    private void disableRefresh() {
        if (rvContent.getParent().getParent() instanceof SwipeRefreshLayout) {
            SwipeRefreshLayout parent = (SwipeRefreshLayout) rvContent.getParent().getParent();
            parent.setEnabled(false);
        }
    }
}
