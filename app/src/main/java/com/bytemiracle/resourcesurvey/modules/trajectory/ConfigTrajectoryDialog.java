package com.bytemiracle.resourcesurvey.modules.trajectory;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.media.paint.ColorPickerDialog;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryConfig;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：轨迹设置界面
 *
 * @author gwwang
 * @date 2021/6/10 9:02
 */
@FragmentTag(name = "轨迹设置")
public class ConfigTrajectoryDialog extends BaseDialogFragment {
    private static final String TAG = "ConfigTrajectoryFragment";

    @BindView(R.id.layout_sampling_width)
    ViewGroup layoutSamplingWidth;
    @BindView(R.id.layout_sampling_rate)
    ViewGroup layoutSamplingRate;
    @BindView(R.id.layout_sampling_type)
    ViewGroup layoutSamplingType;
    @BindView(R.id.layout_trajectory_color)
    ViewGroup layoutTrajectoryColor;
    @BindView(R.id.iv_setup_color)
    ImageView ivSetupColor;
    private DBProject openingProject;
    private TrajectoryConfig trajectoryConfig;

    @Override
    protected void initViews(View view) {
        openingProject = GlobalObjectHolder.getOpeningProject();
        trajectoryConfig = TrajectoryConfigUtils.getConfig(openingProject);
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("保存");
        appTitleController.getRightButton().setOnClickListener(v -> {
            TrajectoryConfigUtils.updateConfig(openingProject, trajectoryConfig);
            dismiss();
        });
        initConfigItem();
    }

    private void initConfigItem() {
        //轨迹线颜色
        ((TextView) layoutTrajectoryColor.findViewById(R.id.tv_flag)).setText("轨迹颜色");
        ivSetupColor.setOnClickListener(v ->
                new ColorPickerDialog(trajectoryConfig.getColor(), pickedColor -> {
                    ivSetupColor.setBackgroundColor(pickedColor);
                    trajectoryConfig.setColor(pickedColor);
                }).show(getChildFragmentManager(), ""));
        //轨迹线宽度
        ((TextView) layoutSamplingWidth.findViewById(R.id.tv_flag)).setText("线条宽度");
        MaterialSpinner spinnerSamplingWidth = (MaterialSpinner) layoutSamplingWidth.findViewById(R.id.spinner);
        spinnerSamplingWidth.setOnItemSelectedListener((view, position, id, item) -> {
            String lineWidth = TrajectoryConfig.LINE_WIDTH_CONFIG[position];
            trajectoryConfig.setLineWidth(Integer.parseInt(lineWidth));
        });
        //采样方式
        ((TextView) layoutSamplingType.findViewById(R.id.tv_flag)).setText("采样方式");
        MaterialSpinner spinnerSamplingType = (MaterialSpinner) layoutSamplingType.findViewById(R.id.spinner);
        //采样率
        ((TextView) layoutSamplingRate.findViewById(R.id.tv_flag)).setText("采样率");
        MaterialSpinner spinnerSamplingRate = (MaterialSpinner) layoutSamplingRate.findViewById(R.id.spinner);

        boolean isTimeInterval = trajectoryConfig.getType() == TrajectoryConfig.TYPE_BY_TIME;
        List<String> typeItems = TrajectoryConfig.SAMPLING_CONFIG.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        spinnerSamplingType.setOnItemSelectedListener((view1, position, id, item) -> {
            String shouldUnit = spinnerSamplingType.getSelectedIndex() == 0 ? "秒" : "米";
            trajectoryConfig.setType(position);
            trajectoryConfig.setSamplingRateUnit(shouldUnit);
            String samplingType = typeItems.get(position);
            List<String> rateItems = TrajectoryConfig.SAMPLING_CONFIG.get(samplingType).stream()
                    .map(integer -> integer + shouldUnit)
                    .collect(Collectors.toList());
            spinnerSamplingRate.setItems(rateItems);
        });
        spinnerSamplingRate.setOnItemSelectedListener((view, position1, id1, item1) -> {
                    String samplingType = typeItems.get(spinnerSamplingType.getSelectedIndex());
                    Integer samplingRate = TrajectoryConfig.SAMPLING_CONFIG.get(samplingType).get(position1);
                    trajectoryConfig.setSamplingRate(samplingRate);
                }
        );
        //初始化数据
        ivSetupColor.setBackgroundColor(trajectoryConfig.getColor());

        spinnerSamplingWidth.setItems(TrajectoryConfig.LINE_WIDTH_CONFIG);
        spinnerSamplingWidth.setSelectedIndex(Arrays.asList(TrajectoryConfig.LINE_WIDTH_CONFIG).indexOf("" + trajectoryConfig.getLineWidth()));

        spinnerSamplingType.setItems(typeItems);
        spinnerSamplingType.setSelectedIndex(isTimeInterval ? 0 : 1);

        String samplingType = typeItems.get(spinnerSamplingType.getSelectedIndex());
        List<String> rateItems = TrajectoryConfig.SAMPLING_CONFIG.get(samplingType).stream()
                .map(integer -> integer + (spinnerSamplingType.getSelectedIndex() == 0 ? "秒" : "米"))
                .collect(Collectors.toList());
        spinnerSamplingRate.setItems(rateItems);
        spinnerSamplingRate.setSelectedIndex(rateItems.indexOf(trajectoryConfig.getSamplingRate() + trajectoryConfig.getSamplingRateUnit()));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_config_trajectory;
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
