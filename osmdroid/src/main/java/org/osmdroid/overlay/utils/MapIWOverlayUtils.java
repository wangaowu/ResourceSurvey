package org.osmdroid.overlay.utils;

import android.util.Log;

import com.bytemiracle.base.framework.utils.common.ListUtils;

import org.osmdroid.overlay.render.PackageOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayWithIW;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：有infoWindow的overlay工具类
 *
 * @author gwwang
 * @date 2022/3/4 10:47
 */
public class MapIWOverlayUtils extends MapOverlayUtils {
    private static final String TAG = "MapIWOverlayUtils";

    /**
     * 自动显示当前的infoWindow
     *
     * @param iwPackageOverlay
     * @param currentMarker
     */
    public static void showSingleInfoWindow(PackageOverlay iwPackageOverlay, Marker currentMarker) {
        if (isShowInfoWindow(iwPackageOverlay)) {
            clearInfoWindows(iwPackageOverlay);
        }
        InfoWindow infoWindow = currentMarker.getInfoWindow();
        if (infoWindow != null && !infoWindow.isOpen()) {
            currentMarker.showInfoWindow();
        }
    }

    /**
     * 是否正在显示infoWindow
     *
     * @param iwPackageOverlay
     * @return
     */
    public static boolean isShowInfoWindow(PackageOverlay iwPackageOverlay) {
        List<OverlayWithIW> shownInfoWindowOverlays = getShownInfoWindowOverlays(iwPackageOverlay);
        return !ListUtils.isEmpty(shownInfoWindowOverlays);
    }

    /**
     * 清空所有的infoWindow
     *
     * @param iwPackageOverlay
     */
    public static void clearInfoWindows(PackageOverlay iwPackageOverlay) {
        List<OverlayWithIW> shownInfoWindowOverlays = getShownInfoWindowOverlays(iwPackageOverlay);
        for (OverlayWithIW shownInfoWindowOverlay : shownInfoWindowOverlays) {
            shownInfoWindowOverlay.closeInfoWindow();
        }
    }

    /**
     * 获取正在显示的InfoWindow图形集合
     *
     * @param iwPackageOverlay
     * @return
     */
    public static List<OverlayWithIW> getShownInfoWindowOverlays(PackageOverlay iwPackageOverlay) {
        List<OverlayWithIW> infoWindowOverlays = getInfoWindowOverlays(iwPackageOverlay);
        return infoWindowOverlays.stream()
                .filter(overlayWithIW -> overlayWithIW.getInfoWindow().isOpen())
                .collect(Collectors.toList());
    }

    /**
     * 获取有绑定InfoWindow图形集合
     *
     * @param iwPackageOverlay
     * @return
     */
    public static List<OverlayWithIW> getInfoWindowOverlays(PackageOverlay iwPackageOverlay) {
        List<OverlayWithIW> overlayWithIWS = new ArrayList<>();
        for (Overlay item : iwPackageOverlay.getItems()) {
            if (item instanceof OverlayWithIW) {
                OverlayWithIW overlayWithIW = (OverlayWithIW) item;
                if (overlayWithIW.getInfoWindow() != null) {
                    overlayWithIWS.add(overlayWithIW);
                }
            }
        }
        return overlayWithIWS;
    }
}
