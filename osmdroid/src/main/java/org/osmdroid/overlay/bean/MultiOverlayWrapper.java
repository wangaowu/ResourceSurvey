package org.osmdroid.overlay.bean;

import org.osmdroid.overlay.render.ISelectOverlay;
import org.osmdroid.overlay.render.PackageOverlay;

import java.util.List;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：支持多面的封装类
 *
 * @author gwwang
 * @date 2022/2/22 16:42
 */
public class MultiOverlayWrapper {

    /**
     * 图层类型
     */
    private OverlayWrapperType overlayWrapperType;
    /**
     * 图层集合(while: overlayWrapperMultiType==OverlayWrapperMultiType.MULTIPLE)
     */
    private List<ISelectOverlay> overlayList;
    /**
     * 图层(while: overlayWrapperMultiType==OverlayWrapperMultiType.SINGLE)
     */
    private ISelectOverlay overlay;

    /**
     * 构造多面对象
     *
     * @param overlayList
     */
    public MultiOverlayWrapper(List<ISelectOverlay> overlayList) {
        this(null, overlayList, OverlayWrapperType.MULTIPLE);
    }

    /**
     * 构造单面对象
     *
     * @param overlay
     */
    public MultiOverlayWrapper(ISelectOverlay overlay) {
        this(overlay, null, OverlayWrapperType.SINGLE);
    }

    /**
     * 构造对象
     *
     * @param overlay
     */
    public MultiOverlayWrapper(ISelectOverlay overlay, List<ISelectOverlay> overlayList, OverlayWrapperType overlayWrapperType) {
        this.overlay = overlay;
        this.overlayList = overlayList;
        this.overlayWrapperType = overlayWrapperType;
    }

    public List<ISelectOverlay> getOverlayList() {
        return overlayList;
    }

    public void setOverlayList(List<ISelectOverlay> overlayList) {
        this.overlayList = overlayList;
    }

    public ISelectOverlay getOverlay() {
        return overlay;
    }

    public void setOverlay(ISelectOverlay overlay) {
        this.overlay = overlay;
    }

    public OverlayWrapperType getOverlayMultiType() {
        return overlayWrapperType;
    }

    public void setOverlayMultiType(OverlayWrapperType overlayWrapperType) {
        this.overlayWrapperType = overlayWrapperType;
    }

    /**
     * 设置overlay的绑定对象
     *
     * @param folderOverlay
     * @param featureRow
     */
    public void setFeatureOverlayInfo(PackageOverlay folderOverlay, FeatureRow featureRow) {
        switch (overlayWrapperType) {
            case SINGLE:
                overlay.getFeatureOverlayInfo().setPackageOverlay(folderOverlay);
                overlay.getFeatureOverlayInfo().setFeatureRow(featureRow);
                break;
            case MULTIPLE:
                setComposeOverlayInfo(featureRow, folderOverlay);
                break;
        }
    }

    //设置多面的绑定对象
    private void setComposeOverlayInfo(FeatureRow featureRow, PackageOverlay folderOverlay) {
        for (ISelectOverlay iSelectOverlay : overlayList) {
            iSelectOverlay.getFeatureOverlayInfo().setFeatureRow(featureRow);
            iSelectOverlay.getFeatureOverlayInfo().setPackageOverlay(folderOverlay);
            iSelectOverlay.setComposeOverlays(overlayList);
        }
    }
}
