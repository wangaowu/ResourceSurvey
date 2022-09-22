package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.ProjectMapOverlayUtils;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.overlay.utils.MapOverlayUtils;

import java.util.List;

import butterknife.BindView;

/**
 * 类功能：编辑图层
 *
 * @author gwwang
 * @date 2021/5/22 15:04
 */
@FragmentTag(name = "编辑图层")
public class EditableLayersFragment extends BaseDialogFragment {

    @BindView(R.id.rv_layers)
    RecyclerView rvLayers;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_editable_layers;
    }

    @Override
    protected void initViews(View view) {
        appTitleController.tvTitle.setText("请单选编辑图层");
        //设置数据
        List<PackageOverlay> editableOverlays = MapElementsHolder.getEditableOverlays();
        if (ListUtils.isEmpty(editableOverlays)) {
            XToastUtils.info("没有可用的图层!");
            dismiss();
            return;
        }
        showMapOverlays(editableOverlays);
    }

    private void showMapOverlays(List<PackageOverlay> editableOverlays) {
        QuickList.instance().adapter(rvLayers, R.layout.item_select_editable_layer, editableOverlays, new QuickListListener<PackageOverlay>() {
            @Override
            public void onBindItem(QuickAdapter<PackageOverlay> quickAdapter, SmartViewHolder holder, PackageOverlay overlay) {
                boolean isEditCurrent = MapElementsHolder.getCurrentEditOverlay() == overlay;
                holder.text(R.id.tv_layer_name, overlay.getName());
                holder.image(R.id.iv_layer_type, ProjectMapOverlayUtils.matchTypeIcon(overlay));
                holder.image(R.id.iv_editable_flag, isEditCurrent ? R.drawable.ic_can_edit_layer : 0);

                //单选时，隐藏其他图层，并将图层缩放到当前的边界
                holder.itemView.setOnClickListener(v -> {
                    MapElementsHolder.setCurrentEditOverlay(overlay);
                    hideOtherLayers(overlay);
                    MapElementsHolder.getMapView().zoomToBoundingBox(overlay.getBounds(), true, MapConstant.DEFAULT_BOX_PADDING);
                    quickAdapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void hideOtherLayers(PackageOverlay overlay) {
        List<PackageOverlay> mapOverlays = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView());
        for (PackageOverlay mapOverlay : mapOverlays) {
            if (mapOverlay != overlay) {
                mapOverlay.setEnabled(false);
            }
        }
        overlay.setEnabled(true);
    }
}
