package org.osmdroid.overlay.render;

import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.views.overlay.FolderOverlay;

/**
 * 类功能：自管理的overlay
 *
 * @author gwwang
 * @date 2022/2/26 9:12
 */
public class PackageOverlay extends FolderOverlay {
    private PackageOverlayInfo packageOverlayInfo;

    public PackageOverlay() {
        this(null);
    }

    public PackageOverlay(String overlayName) {
        setName(overlayName);
    }

    public PackageOverlayInfo getPackageOverlayInfo() {
        return packageOverlayInfo;
    }

    public void setPackageOverlayInfo(PackageOverlayInfo packageOverlayInfo) {
        this.packageOverlayInfo = packageOverlayInfo;
    }

    @Override
    public String toString() {
        return "PackageOverlay[" + getName() + "]_hashCode{" + hashCode() + "},packageOverlayInfo: " + packageOverlayInfo.toString();
    }
}
