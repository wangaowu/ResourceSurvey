package org.jts.utils;

import java.math.BigDecimal;

/**
 * 类功能：仅供组件使用
 *
 * @author gwwang
 * @date 2022/2/8 14:38
 */
public class PrivateNumberUtils {

    private static boolean isPlainText(String src) {
        return !src.contains("E") && !src.contains("e");
    }

    public static double getDouble(double src, int floatCount) {
        if (!isPlainText(src + "")) {
            return getPlainDouble(src, floatCount);
        } else {
            BigDecimal b = new BigDecimal(src);
            return b.setScale(floatCount, 4).doubleValue();
        }
    }

    public static double getPlainDouble(double src, int floatCount) {
        BigDecimal b = new BigDecimal(src + "");
        b = new BigDecimal(b.toPlainString());
        return b.setScale(floatCount, 4).doubleValue();
    }
}
