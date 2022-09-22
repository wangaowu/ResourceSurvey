package com.bytemiracle.resourcesurvey.modules.trajectory;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import java.util.Arrays;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：修正轨迹参数配置界面
 *
 * @author gwwang
 * @date 2021/6/10 9:02
 */
@FragmentTag(name = "修正轨迹")
public class FixTrajectoryRecordDialog extends BaseDialogFragment {
    private static final String TAG = "FixTrajectoryRecordDialog";
    //单位：个
    public static final String[] CONFIG_MIN_COUNT_ITEMS = new String[]{"10", "30", "50", "100"};
    //单位：米
    public static final String[] CONFIG_MIN_DISTANCE_ITEMS = new String[]{"10", "50", "10", "300"};

    @BindView(R.id.layout_config_min_count)
    ViewGroup layoutConfigMinCount;
    @BindView(R.id.layout_config_min_distance)
    ViewGroup layoutConfigMinDistance;
    @BindView(R.id.tv_fix_info)
    TextView tvFixedInfo;
    @BindView(R.id.btn_fix)
    Button btnFix;

    private TrajectoryBizz trajectoryBizz;
    private CommonAsyncListener notifyUpdateListListener;

    private int configMinLimitDistance = TrajectoryBizz.MIN_LIMIT_MOVE_DISTANCE;
    private int configMinLimitCount = TrajectoryBizz.MIN_LIMIT_POINT_COUNT;

    public FixTrajectoryRecordDialog(TrajectoryBizz trajectoryBizz, CommonAsyncListener notifyUpdateListListener) {
        this.trajectoryBizz = trajectoryBizz;
        this.notifyUpdateListListener = notifyUpdateListListener;
    }

    @Override
    protected void initViews(View view) {
        initConfigItem();
        btnFix.setOnClickListener(this::fixTrajectoryRecord);
    }

    private void initConfigItem() {
        //轨迹点数量
        ((TextView) layoutConfigMinCount.findViewById(R.id.tv_flag)).setText("过滤数量");
        MaterialSpinner spinnerConfigMinCount = (MaterialSpinner) layoutConfigMinCount.findViewById(R.id.spinner);
        spinnerConfigMinCount.setOnItemSelectedListener((view, position, id, item) -> {
            configMinLimitCount = Integer.parseInt(CONFIG_MIN_COUNT_ITEMS[position]);
            updateFixInfo();
        });
        //轨迹移动距离
        ((TextView) layoutConfigMinDistance.findViewById(R.id.tv_flag)).setText("过滤距离");
        MaterialSpinner spinnerConfigMinDistance = (MaterialSpinner) layoutConfigMinDistance.findViewById(R.id.spinner);
        spinnerConfigMinDistance.setOnItemSelectedListener((view, position, id, item) -> {
            configMinLimitDistance = Integer.parseInt(CONFIG_MIN_DISTANCE_ITEMS[position]);
            updateFixInfo();
        });

        spinnerConfigMinCount.setItems(Arrays.stream(CONFIG_MIN_COUNT_ITEMS).map(e -> e + "个").collect(Collectors.toList()));
        spinnerConfigMinDistance.setItems(Arrays.stream(CONFIG_MIN_DISTANCE_ITEMS).map(e -> e + "米").collect(Collectors.toList()));

        //初始化数据
        spinnerConfigMinCount.setSelectedIndex(Arrays.asList(CONFIG_MIN_COUNT_ITEMS).indexOf("" + configMinLimitCount));
        spinnerConfigMinDistance.setSelectedIndex(Arrays.asList(CONFIG_MIN_DISTANCE_ITEMS).indexOf("" + configMinLimitDistance));
        updateFixInfo();
    }

    private void fixTrajectoryRecord(View view) {
        trajectoryBizz.fixTrajectoryRecord(configMinLimitCount, configMinLimitDistance);
        notifyUpdateListListener.doSomething(new Object());
        dismiss();
    }

    private void updateFixInfo() {
        tvFixedInfo.setText(getString(R.string.trajectory_fix_info,
                configMinLimitCount + "个",
                configMinLimitDistance + "米"));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_fix_trajectory_record;
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }

    @Override
    protected float getHeightRatio() {
        return .35f;
    }
}
