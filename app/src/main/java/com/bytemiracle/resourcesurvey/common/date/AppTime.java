package com.bytemiracle.resourcesurvey.common.date;

import com.xuexiang.xutil.data.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 类功能：app统一显示时间处理
 *
 * @author gwwang
 * @date 2021/5/27 10:17
 */
public class AppTime {

    private static final SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY/MM/dd");

    /**
     * 格式化时间字符串
     *
     * @param timestamps
     * @return
     */
    public static String formatTimestamps(long timestamps) {
        return formatDateTime(new Date(timestamps));
    }

    /**
     * 格式化时间字符串
     *
     * @param date
     * @return
     */
    public static String formatDateTime(Date date) {
        return simpleDateFormatTime.format(date);
    }

    /**
     * 格式化时间字符串
     *
     * @param timeString
     * @return
     */
    public static Date formatTime(String timeString) {
        long timestamps = DateUtils.string2Millis(timeString, simpleDateFormatTime);
        return new Date(timestamps);
    }

    /**
     * 格式化日期字符串
     *
     * @param timestamps
     * @return
     */
    public static String formatDate(long timestamps) {
        return formatDateTime(new Date(timestamps));
    }

    /**
     * 格式化日期字符串
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    /**
     * 格式化日期字符串
     *
     * @param timeString
     * @return
     */
    public static Date formatDate(String timeString) {
        long timestamps = DateUtils.string2Millis(timeString, simpleDateFormat);
        return new Date(timestamps);
    }
}
