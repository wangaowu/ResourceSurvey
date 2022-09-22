package com.bytemiracle.resourcesurvey.giscommon.location.utils;


import android.util.Pair;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/3/29 10:49
 */
public class LocalCoordSysConvertUtils {

    public static double pi = 3.1415926535897932384626;
    public static double a = 6378140.0;//1975年国际椭球体长半轴
    public static double ee = 0.0033528131778969143;//1975年国际椭球体扁率

    /**
     * CGCS2000坐标系与WGS84坐标系的差异
     * <p>
     * CGCS2000是2000国家大地坐标系，该系统以ITRF(国际协议地球参考框架)97参考框架为基准，参考框架历元为0。当前，国际地球参考系（ITRS）和国际地球参考框架（ITRF）是世界上最精确、最权威的地心大地坐标系
     * 它们都是地心坐标系，坐标原点都在(包含地球周围大气的)地球质心
     * 它们的初始参数都来源于GRS(1980)椭球；
     * 大地坐标系有4个主要几何参数，两者有3个相同，分别是长半轴，地心引力常数，自转角速度。只有扁率f不同，CGCS2000是f=1/298.257222101，WGS84是1/298.257223563。由此看出两者之间参数定义的区别是很小的，而这一点区别到底有多影响呢，程院长的论文（《2000 国家大地坐标系椭球参数与GRS80和WGS84的比较》）给出的一个数字是：“给定点位在某一框架和某一历元下的空间直角坐标，投影到CGCS2000椭球和WGS84椭球上所得的纬度的最大差异相当于11mm。”
     * CGCS2000的定义与WGS84采用的参考椭球非常接近。扁率差异引起椭球面上的纬度和高度变化最大达1mm。当前测量精度范围内，可以忽略这点差异。可以说两者相容至cm级水平，但若一点的坐标精度达不到cm水平，则不认为CGCS2000和WGS84的坐标是相容的。
     * 高精度地心坐标必须考虑板块运动影响，由于地球内部地壳运动，坐标参考框架都是动态维持的，其参数需要定期更新。
     *
     * @param lon
     * @param lat
     * @return
     */
    public static Pair<Double, Double> GPS84ToGCJ2000(double lon, double lat) {
        return new Pair<Double, Double>(lon, lat);
    }

    public static Pair<Double, Double> GPS84ToGCJ02(double lon, double lat) {
        if (outOfChina(lon, lat)) {
            return null;
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Pair<Double, Double>(mgLon, mgLat);
    }

    public static Pair<Double, Double> GCJ02ToGPS84(double lon, double lat) {
        Pair<Double, Double> point = transform(lon, lat);
        double lontitude = lon * 2 - point.first;
        double latitude = lat * 2 - point.second;
        return new Pair<Double, Double>(lontitude, latitude);
    }

    private static boolean outOfChina(double lon, double lat) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        return lat < 0.8293 || lat > 55.8271;
    }

    private static Pair<Double, Double> transform(double lon, double lat) {
        if (outOfChina(lon, lat)) {
            return new Pair<Double, Double>(lon, lat);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Pair<Double, Double>(mgLon, mgLat);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }
}
