package com.bytemiracle.resourcesurvey.modules.settings;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyle;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.bytemiracle.resourcesurvey.modules.media.paint.ColorPickerDialog;
import com.xuexiang.xui.widget.spinner.materialspinner.MaterialSpinner;

import org.osmdroid.overlay.render.PackageOverlay;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：样式设置界面
 *
 * @author gwwang
 * @date 2021/6/10 9:02
 */
@FragmentTag(name = "样式设置")
public class ConfigFeatureStyleDialog extends BaseDialogFragment {
    private static final String TAG = "ConfigFeatureStyleDialog";
    private static final String[] ITEM_BOUND_WIDTH = {"1", "2", "3", "4"};
    private static final String[] ITEM_ALPHAS = {"0.25", "0.50", "0.75", "1.00"};

    @BindView(R.id.layout_solid_color)
    ViewGroup layoutSolidColor;
    @BindView(R.id.layout_bound_color)
    ViewGroup layoutBoundColor;
    @BindView(R.id.layout_solid_alpha)
    ViewGroup layoutSolidAlpha;
    @BindView(R.id.layout_bound_width)
    ViewGroup layoutBoundWidth;
    @BindView(R.id.layout_select_bound_color)
    ViewGroup layoutSelectBoundColor;
    @BindView(R.id.layout_select_mark_field)
    ViewGroup layoutSelectMarkField;
    @BindView(R.id.layout_select_bound_width)
    ViewGroup layoutSelectBoundWidth;
    @BindView(R.id.layout_select_solid_color)
    ViewGroup layoutSelectSolidColor;

    private OverlayRenderStyle overlayRenderStyle;
    private PackageOverlay overlay;
    private CommonAsyncListener<ConfigFeatureStyleDialog> onStyleChangeListener;

    public ConfigFeatureStyleDialog(PackageOverlay overlay, CommonAsyncListener<ConfigFeatureStyleDialog> onStyleChangeListener) {
        this.overlay = overlay;
        this.onStyleChangeListener = onStyleChangeListener;
    }

    @Override
    protected void initViews(View view) {
        overlayRenderStyle = OverlayRenderStyleUtils.getOverlayStyle(overlay.getName());
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("保存");
        appTitleController.getRightButton().setOnClickListener(v -> {
            OverlayRenderStyleUtils.updateOverlayRenderStyle(overlay.getName(), overlayRenderStyle);
            onStyleChangeListener.doSomething(ConfigFeatureStyleDialog.this);
        });
        overlay.getPackageOverlayInfo().openWritableGeoPackage(geoPackage -> {
            List<String> featureColumnNames = geoPackage.getFeatureDao(overlay.getName()).getColumns().stream()
                    .filter(featureColumn -> !featureColumn.isGeometry() && !featureColumn.isPrimaryKey())
                    .map(featureColumn -> featureColumn.getName())
                    .collect(Collectors.toList());
            featureColumnNames.add(0, "请选择");
            String[] fieldNameArray = new String[featureColumnNames.size()];
            fieldNameArray = featureColumnNames.toArray(fieldNameArray);
            initConfigItem(fieldNameArray);
        });
    }

    private void initConfigItem(String[] fieldNameArray) {
        //实体填充颜色
        ((TextView) layoutSolidColor.findViewById(R.id.tv_flag)).setText("填充颜色");
        ImageView ivSolidColor = layoutSolidColor.findViewById(R.id.iv_setup_color);
        ivSolidColor.setOnClickListener(v ->
                new ColorPickerDialog(overlayRenderStyle.getFeatureSolidColor(), pickedColor -> {
                    overlayRenderStyle.setFeatureSolidColor(pickedColor);
                    ivSolidColor.setBackgroundColor(pickedColor);
                }).show(getChildFragmentManager(), ""));
        //实体边线颜色
        ((TextView) layoutBoundColor.findViewById(R.id.tv_flag)).setText("边线颜色");
        ImageView ivBoundColor = layoutBoundColor.findViewById(R.id.iv_setup_color);
        ivBoundColor.setOnClickListener(v ->
                new ColorPickerDialog(overlayRenderStyle.getFeatureBoundColor(), pickedColor -> {
                    overlayRenderStyle.setFeatureBoundColor(pickedColor);
                    ivBoundColor.setBackgroundColor(pickedColor);
                }).show(getChildFragmentManager(), ""));
        //配置全局填充所需的透明度
        MaterialSpinner spinnerSolidAlpha = layoutSolidAlpha.findViewById(R.id.spinner);
        ((TextView) layoutSolidAlpha.findViewById(R.id.tv_flag)).setText("填充透明度");
        spinnerSolidAlpha.setOnItemSelectedListener((view, position, id, item) -> {
            overlayRenderStyle.setFeatureSolidAlpha(Double.parseDouble(ITEM_ALPHAS[position]));
        });
        //配置边线宽度
        MaterialSpinner spinnerBoundWidth = layoutBoundWidth.findViewById(R.id.spinner);
        ((TextView) layoutBoundWidth.findViewById(R.id.tv_flag)).setText("边线宽度");
        spinnerBoundWidth.setOnItemSelectedListener((view, position, id, item) -> {
            overlayRenderStyle.setFeatureBoundLineWidth(Integer.valueOf(ITEM_BOUND_WIDTH[position]));
        });
        //选中实体填充颜色
        ((TextView) layoutSelectSolidColor.findViewById(R.id.tv_flag)).setText("选中填充颜色");
        ImageView ivSelectSolidColor = layoutSelectSolidColor.findViewById(R.id.iv_setup_color);
        ivSelectSolidColor.setOnClickListener(v ->
                new ColorPickerDialog(overlayRenderStyle.getSelectSolidColor(), pickedColor -> {
                    overlayRenderStyle.setSelectSolidColor(pickedColor);
                    ivSelectSolidColor.setBackgroundColor(pickedColor);
                }).show(getChildFragmentManager(), ""));
        //选中边线颜色
        ((TextView) layoutSelectBoundColor.findViewById(R.id.tv_flag)).setText("选中边线颜色");
        ImageView ivSelectBoundColor = layoutSelectBoundColor.findViewById(R.id.iv_setup_color);
        ivSelectBoundColor.setOnClickListener(v ->
                new ColorPickerDialog(overlayRenderStyle.getFeatureSolidColor(), pickedColor -> {
                    overlayRenderStyle.setSelectBoundColor(pickedColor);
                    ivSelectBoundColor.setBackgroundColor(pickedColor);
                }).show(getChildFragmentManager(), ""));
        //配置选中边线宽度
        MaterialSpinner spinnerSelectBoundWidth = layoutSelectBoundWidth.findViewById(R.id.spinner);
        ((TextView) layoutSelectBoundWidth.findViewById(R.id.tv_flag)).setText("选中边线宽度");
        spinnerSelectBoundWidth.setOnItemSelectedListener((view, position, id, item) -> {
            overlayRenderStyle.setSelectBoundLineWidth(Integer.valueOf(ITEM_BOUND_WIDTH[position]));
        });
        //选中要不要使用标注（当前仅支持polygon类型）
        MaterialSpinner spinnerSelectGridStyle = layoutSelectMarkField.findViewById(R.id.spinner);
        ((TextView) layoutSelectMarkField.findViewById(R.id.tv_flag)).setText("标注字段");
        spinnerSelectGridStyle.setOnItemSelectedListener((view, position, id, item) -> {
            boolean useMark = !"请选择".equals(fieldNameArray[position]);
            overlayRenderStyle.setMarkFieldName(useMark ? fieldNameArray[position] : "");
        });

        //初始化数据
        ivSolidColor.setBackgroundColor(overlayRenderStyle.getFeatureSolidColor());
        ivSelectBoundColor.setBackgroundColor(overlayRenderStyle.getSelectBoundColor());
        ivBoundColor.setBackgroundColor(overlayRenderStyle.getFeatureBoundColor());
        ivSelectSolidColor.setBackgroundColor(overlayRenderStyle.getSelectSolidColor());

        spinnerBoundWidth.setItems(ITEM_BOUND_WIDTH);
        spinnerBoundWidth.setSelectedIndex(Arrays.asList(ITEM_BOUND_WIDTH).indexOf("" + overlayRenderStyle.getFeatureBoundLineWidth()));

        spinnerSolidAlpha.setItems(ITEM_ALPHAS);
        spinnerSolidAlpha.setSelectedIndex(getAlphaTextIndex(overlayRenderStyle.getFeatureSolidAlpha()));

        spinnerSelectBoundWidth.setItems(ITEM_BOUND_WIDTH);
        spinnerSelectBoundWidth.setSelectedIndex(Arrays.asList(ITEM_BOUND_WIDTH).indexOf("" + overlayRenderStyle.getSelectBoundLineWidth()));

        spinnerSelectGridStyle.setItems(fieldNameArray);
        String markFieldName = overlayRenderStyle.getMarkFieldName();
        spinnerSelectGridStyle.setSelectedIndex(TextUtils.isEmpty(markFieldName) ? 0 : Arrays.asList(fieldNameArray).indexOf(markFieldName));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_config_feature_style;
    }

    private int getAlphaTextIndex(double featureSolidAlpha) {
        for (int index = 0; index < ITEM_ALPHAS.length; index++) {
            if (Double.parseDouble(ITEM_ALPHAS[index]) == featureSolidAlpha) {
                return index;
            }
        }
        return ITEM_ALPHAS.length - 1;
    }

    @Override
    protected float getWidthRatio() {
        return .5f;
    }
}
