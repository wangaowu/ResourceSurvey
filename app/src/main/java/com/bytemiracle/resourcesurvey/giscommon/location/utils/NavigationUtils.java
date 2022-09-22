package com.bytemiracle.resourcesurvey.giscommon.location.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.xuexiang.xui.widget.dialog.materialdialog.MaterialDialog;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.Arrays;
import java.util.List;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/4/14 10:33
 */
public class NavigationUtils {

    public static void showDialog(Context context, GeoPoint endPoint) {
        new MaterialDialog.Builder(context)
//                        .title("导航软件")
                .items("百度地图", "高德地图")
                .cancelable(true)
                .itemsCallback((dialog, view, which, text) -> {
                    if (which == 0) {
                        NavigationUtils.openBaiduMap(context, endPoint.getLatitude(), endPoint.getLongitude());
                    } else {
                        NavigationUtils.openGaoDeMap(context, endPoint.getLatitude(), endPoint.getLongitude());
                    }
                })
                .show();
    }

    /**
     * 打开高德地图（公交出行，起点位置使用地图当前位置）
     * navi  服务类型
     * sourceApplication 第三方调用应用名称。如 amap
     * lat  纬度
     * lon  经度
     * dev  是否偏移(0:lat 和 lon 是已经加密后的,不需要国测加密; 1:需要国测加密)
     * style
     * 导航方式（0 速度快；1 费用少；2路程短；3 不走高速；4 躲避拥堵；5 不走高速且避免收费；6 不走高速且躲避拥堵；7；躲避收费和拥堵；8 不走高速躲避收费和拥堵）
     * 由于与用户本地设置冲突，Android平台自8.2.6版本起不支持该参数，偏好设置将以用户本地设置为准
     * t = 0（驾车）= 1（公交）= 2（步行）= 3（骑行）= 4（火车）= 5（长途客车）
     * androidamap://navi?sourceApplication=appname&poiname=fangheng&lat=36.547901&lon=104.258354&dev=1&style=2
     *
     * @param dlat 终点纬度
     * @param dlon 终点经度
     */
    public static void openGaoDeMap(Context context, double dlat, double dlon) {
        if (checkMapAppsIsExist(context, "com.autonavi.minimap")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.autonavi.minimap");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setData(Uri.parse("androidamap://route?sourceApplication=" + R.string.app_name
                    + "&sname=我的位置&dlat=" + dlat
                    + "&dlon=" + dlon
//                    + "&dname=" + dname
                    + "&dev=1&m=0&t=0"));
            context.startActivity(intent);
        } else {
            XToastUtils.warning("高德地图未安装");
        }
    }

    /**
     * 打开百度地图（公交出行，起点位置使用地图当前位置）
     * origin=我的位置
     * mode = transit（公交）、driving（驾车）、walking（步行）和riding（骑行）. 默认:driving
     * 当 mode=transit 时 ： sy = 0：推荐路线 、 2：少换乘 、 3：少步行 、 4：不坐地铁 、 5：时间短 、 6：地铁优先
     *
     * @param dlat 终点纬度
     * @param dlon 终点经度
     */
    public static void openBaiduMap(Context context, double dlat, double dlon) {
        if (checkMapAppsIsExist(context, "com.baidu.BaiduMap.auto")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("baidumap://map/direction?origin=我的位置&destination="
//                    + dname
                    + dlat + "," + dlon
                    + "&mode=driving&sy=3&index=0&target=1&coord_type=wgs84&src=andr.bytemiracle.project"));
//                    + "&mode=driving&sy=3&index=0&target=1"));
            context.startActivity(intent);
        } else {
            XToastUtils.warning("百度地图未安装");
        }
    }

    /**
     * 打开腾讯地图（公交出行，起点位置使用地图当前位置）
     * <p>
     * 公交：type=bus，policy有以下取值
     * 0：较快捷 、 1：少换乘 、 2：少步行 、 3：不坐地铁
     * 驾车：type=drive，policy有以下取值
     * 0：较快捷 、 1：无高速 、 2：距离短
     * policy的取值缺省为0
     *
     * @param dlat  终点纬度
     * @param dlon  终点经度
     * @param dname 终点名称
     */
    private void openTencent(double dlat, double dlon, String dname) {
        if (checkMapAppsIsExist(GlobalInstanceHolder.applicationContext(), "com.tencent.map")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("qqmap://map/routeplan?type=drive&from=我的位置&fromcoord=0,0"
                    + "&to=" + dname
                    + "&tocoord=" + dlat + "," + dlon
                    + "&policy=0&referer=myapp"));
            GlobalInstanceHolder.applicationContext().startActivity(intent);
        } else {
            XToastUtils.info("腾讯地图未安装");
        }
    }

    /**
     * 检测地图应用是否安装
     *
     * @param context
     * @param packagename
     * @return
     */
    public static boolean checkMapAppsIsExist(Context context, String packagename) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    /**
     * 求所有点之间的距离
     *
     * @param points
     * @return
     */
    public static double getDistance(List<GeoPoint> points) {
        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        return polyline.getDistance();
    }

    /**
     * 求两点之间距离
     *
     * @return
     */
    public static double getDistance(GeoPoint point1, GeoPoint point2) {
        return getDistance(Arrays.asList(point1, point2));
    }
}
