package com.bytemiracle.resourcesurvey.modules.settings;

import androidx.appcompat.app.AlertDialog;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.dynamicitem.BaseDynamicItemFragment;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemData;
import com.bytemiracle.base.framework.update.UpdatePresenter;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.ConfigLayerFieldPoolFragment;
import com.bytemiracle.resourcesurvey.modules.splash.SplashActivity;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.osmdroid.overlay.utils.MapOverlayUtils;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 类功能：设置界面
 *
 * @author gwwang
 * @date 2021/5/21 15:14
 */
@FragmentTag(name = "设置界面")
public class SettingsFragment extends BaseDynamicItemFragment {

    @Override
    protected void initViews() {
        super.initViews();
        initItems();
    }

    private void initItems() {
        addItem(new ItemData(ItemData.ItemType.ITEM_CLICKABLE, "配置可选字典").centerIcon(R.drawable.ic_image_style).itemClickListener(v -> {
            //配置字典
            List<String> overlayNames = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView()).stream()
                    .map(overlay -> overlay.getName())
                    .collect(Collectors.toList());
            new ConfigLayerFieldPoolFragment(overlayNames).show(getChildFragmentManager(), "");
        }));
        addItem(new ItemData(ItemData.ItemType.ITEM_CLICKABLE, "关于").centerIcon(R.drawable.ic_about).itemClickListener(v -> {
            showVersionInfo();
        }));
    }

    private void showVersionInfo() {
        String versionName = new UpdatePresenter((BaseActivity) getActivity()).getVersionName();
        String packageTime = "2022" + versionName.substring(versionName.length() - 4);
        long validTime = SplashActivity.VALID_TIME;
        long year = validTime / 60 / 60 / 24 / 365;
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.app_name))
                .setIcon(R.drawable.ic_launcher)
                .setMessage(
                        "当前版本: " + versionName + "\n"
                                + "打包日期: " + packageTime + "\n"
                                + "\n"
                                + "已授权: " + "Micky" + "\n"
                                + "授权时间: " + year + "年"
                )
                .setCancelable(true)
                .create().show();
    }
}
