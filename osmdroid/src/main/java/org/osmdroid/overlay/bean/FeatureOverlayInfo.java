package org.osmdroid.overlay.bean;

import org.osmdroid.overlay.render.PackageOverlay;

import mil.nga.geopackage.features.user.FeatureRow;

/**
 * 类功能：具体图形的伴随信息对象
 * (OverlayWithIW::setRelatedObject(IWDescription object))
 *
 * @author gwwang
 * @date 2022/2/22 14:09
 */
public class FeatureOverlayInfo {
    /**
     * gpkg：：featureRow信息
     */
    private FeatureRow featureRow;
    /**
     * 所处图层的信息
     */
    private PackageOverlay packageOverlay;

    public FeatureOverlayInfo(PackageOverlay packageOverlay, FeatureRow featureRow) {
        this.packageOverlay = packageOverlay;
        this.featureRow = featureRow;
    }

    public FeatureRow getFeatureRow() {
        return featureRow;
    }

    public void setFeatureRow(FeatureRow featureRow) {
        this.featureRow = featureRow;
    }

    public PackageOverlay getPackageOverlay() {
        return packageOverlay;
    }

    public void setPackageOverlay(PackageOverlay packageOverlay) {
        this.packageOverlay = packageOverlay;
    }
}
