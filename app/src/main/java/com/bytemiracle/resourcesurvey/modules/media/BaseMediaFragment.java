package com.bytemiracle.resourcesurvey.modules.media;

import com.bytemiracle.base.framework.fragment.BaseFragment;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;

import org.osmdroid.overlay.bean.FeatureOverlayInfo;
import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.views.overlay.OverlayWithIW;

import java.io.File;

/**
 * 类功能：多媒体管理基类
 *
 * @author gwwang
 * @date 2021/6/8 16:07
 */
public abstract class BaseMediaFragment extends BaseFragment {
    /**
     * 首页地图选中的feature
     * see:
     * null----工程多媒体
     * ！null---- feature多媒体
     */
    protected OverlayWithIW identifyShape;
    private File mediaPath;

    @Override
    protected void initViews() {
        identifyShape = ((MediaPropertyFragment) getParentFragment()).getIdentifyShape();
        mediaPath = ProjectUtils.Media.getMediaPath(GlobalObjectHolder.getOpeningProject().getName());
    }

    /**
     * 获取多媒体配置路径
     *
     * @param mediaType 多媒体类型
     * @return
     */
    protected File getConfigMediaDir(String mediaType) {
        String projectMediaPath = mediaPath + File.separator + mediaType;
        if (identifyShape == null) {
            return new File(projectMediaPath);
        }
        FeatureOverlayInfo featureOverlayInfo = ((ISelectOverlay) identifyShape).getFeatureOverlayInfo();
        long id = featureOverlayInfo.getFeatureRow().getId();
        String layerName = featureOverlayInfo.getPackageOverlay().getName();
        File layerFeatureDir = new File(projectMediaPath + File.separator + layerName + File.separator + id);
        if (!layerFeatureDir.exists()) {
            layerFeatureDir.mkdirs();
        }
        return layerFeatureDir;
    }
}
