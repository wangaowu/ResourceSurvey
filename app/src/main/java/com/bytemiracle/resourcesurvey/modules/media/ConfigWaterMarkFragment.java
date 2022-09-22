package com.bytemiracle.resourcesurvey.modules.media;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.watermark.WaterMarkUtils;
import com.bytemiracle.resourcesurvey.modules.media.paint.ColorPickerDialog;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.xuexiang.xui.widget.button.switchbutton.SwitchButton;

import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：水印设置界面
 *
 * @author gwwang
 * @date 2021/6/10 9:02
 */
@FragmentTag(name = "水印设置")
public class ConfigWaterMarkFragment extends BaseDialogFragment {
    private static final String TAG = "SetWaterMarkFragment";

    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.iv_setup_color)
    ImageView ivSetupColor;

    @Override
    protected void initViews(View view) {
        int watermarkColor = Integer.parseInt(WaterMarkUtils.getSpecialValue("颜色"));
        ivSetupColor.setBackgroundColor(watermarkColor);
        ivSetupColor.setOnClickListener(v ->
                new ColorPickerDialog(watermarkColor, pickedColor -> {
                    ivSetupColor.setBackgroundColor(pickedColor);
                    WaterMarkUtils.setNewConfigValue("颜色", true, String.valueOf(pickedColor));
                }).show(getChildFragmentManager(), ""));
        updateWatermarkAdapter();
    }

    private void updateWatermarkAdapter() {
        List<String> watermarkConfigFields = WaterMarkUtils.getWatermarkConfigFields()
                .stream()
                .filter(s -> !s.equals("颜色"))
                .collect(Collectors.toList());
        QuickList.instance().adapter(rvContent, R.layout.item_watermark_config, watermarkConfigFields, new QuickListListener<String>() {
            @Override
            public void onBindItem(QuickAdapter<String> quickAdapter, SmartViewHolder h, String watermarkName) {
                h.text(R.id.tv_watermark_name, watermarkName);

                SwitchButton switchButton = (SwitchButton) h.findView(R.id.sb_setup_state);
                boolean setupState = WaterMarkUtils.getSetupState(watermarkName);
                switchButton.setCheckedNoEvent(setupState);

                switchButton.setClickable(WaterMarkUtils.isAllowChangeDefaultSetup(watermarkName));
                switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    //设置最新的开关状态
                    WaterMarkUtils.setNewConfigValue(watermarkName, isChecked, null);
                });
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_config_watermark;
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }

    @Override
    protected float getHeightRatio() {
        return .4f;
    }
}
