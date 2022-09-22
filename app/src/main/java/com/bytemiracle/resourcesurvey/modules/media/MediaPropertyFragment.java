package com.bytemiracle.resourcesurvey.modules.media;

import android.view.View;

import com.bytemiracle.base.framework.fragment.CoreFragmentManager;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.view.BaseCheckPojo;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.view.AppTabLayout;
import com.bytemiracle.resourcesurvey.modules.media.audio.MediaAudioFragment;
import com.bytemiracle.resourcesurvey.modules.media.paint.MediaCustomPaintFragment;
import com.bytemiracle.resourcesurvey.modules.media.picture.MediaPictureFragment;
import com.bytemiracle.resourcesurvey.modules.media.video.MediaVideoFragment;

import org.osmdroid.overlay.render.IWPolygon;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * 类功能：多媒体
 *
 * @author gwwang
 * @date 2021/5/22 15:04
 */
@FragmentTag(name = "多媒体")
public class MediaPropertyFragment extends BaseDialogFragment {
    private static final String TAG = "MediaPropertyFragment";

    private final List<AppTabLayout.Pojo> TOP_TABS = Arrays.asList(new AppTabLayout.Pojo[]{
            new AppTabLayout.Pojo(0, "图片", MediaPictureFragment.class, false, false),
            new AppTabLayout.Pojo(0, "视频", MediaVideoFragment.class, false, false),
            new AppTabLayout.Pojo(0, "录音", MediaAudioFragment.class, false, false),
            new AppTabLayout.Pojo(0, "绘图", MediaCustomPaintFragment.class, false, false)});

    @BindView(R.id.tabs_top)
    AppTabLayout tabsTop;

    private CoreFragmentManager coreFragmentManager;
    private OverlayWithIW identifyShape;

    public MediaPropertyFragment(OverlayWithIW identifyShape) {
        this.identifyShape = identifyShape;
    }

    public OverlayWithIW getIdentifyShape() {
        return identifyShape;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_media_property;
    }

    @Override
    protected void initViews(View view) {
        coreFragmentManager = CoreFragmentManager.newInstance(this, R.id.fl_container);
        initTopBarListener();
        updateTopTabs();
        tabsTop.checkFirst();
    }

    private void initTopBarListener() {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("水印设置");
        appTitleController.getRightButton().setOnClickListener(v -> new ConfigWaterMarkFragment().show(getChildFragmentManager(), ""));
    }

    private void updateTopTabs() {
        tabsTop.initTabs(TOP_TABS, (pojo, isClick) -> {
            if (isClick) {
                //使用数据驱动单选操作
                BaseCheckPojo.clearCheckedItem(TOP_TABS);
                pojo.setChecked(true);
                updateTopTabs();
            }
            if (pojo.isChecked()) {
                coreFragmentManager.switch2Fragment(pojo.fragmentClazz);
            }
        });
    }
}
