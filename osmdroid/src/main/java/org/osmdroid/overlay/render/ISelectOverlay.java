package org.osmdroid.overlay.render;

import org.jetbrains.annotations.NotNull;
import org.osmdroid.overlay.bean.FeatureOverlayInfo;

import java.util.List;

/**
 * 类功能：可选中
 *
 * @author gwwang
 * @date 2022/2/25 8:47
 */
public interface ISelectOverlay<T> {

    void setSelected(boolean selected);

    boolean isSelected();

    FeatureOverlayInfo getFeatureOverlayInfo();

    void setFeatureOverlayInfo(FeatureOverlayInfo featureOverlayInfo);

    List<ISelectOverlay> getComposeOverlays();

    void setComposeOverlays(List<ISelectOverlay> composeOverlays);

    void setSelectOptions(@NotNull T options);
}
