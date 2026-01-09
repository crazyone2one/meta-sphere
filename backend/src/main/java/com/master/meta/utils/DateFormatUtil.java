package com.master.meta.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;


/**
 * @author Created by 11's papa on 2025/10/15
 */
public class DateFormatUtil {
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static Date getDate(String dateString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.parse(dateString);
    }

    /**
     * 返回yyyyMMddHHmmss格式的时间
     *
     * @param localDateTime 需要转换的时间
     * @return java.lang.String
     */
    public static String localDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return localDateTime.format(formatter);
    }

    /**
     * 返回yyyy-MM-dd HH:mm:ss格式的时间
     *
     * @param localDateTime 需要转换的时间
     * @return java.lang.String
     */
    public static String localDateTime2StringStyle2(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        return localDateTime.format(formatter);
    }

    /**
     * 返回yyyy-MM-dd格式的时间
     *
     * @param localDateTime 需要转换的时间
     * @return java.lang.String
     */
    public static String localDateTime2StringStyle3(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return localDateTime.format(formatter);
    }

    public static String localDate2String(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return localDate.format(formatter);
    }

    /**
     * 将yyyyMMddHHmmss格式的字符串转换为LocalDateTime
     *
     * @param dateTimeStr 需要转换的时间字符串
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime string2LocalDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss格式的字符串转换为LocalDateTime
     *
     * @param dateTimeStr 需要转换的时间字符串
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime string2LocalDateTimeStyle2(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(TIME_PATTERN);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 将yyyy-MM-dd格式的字符串转换为LocalDateTime
     *
     * @param dateStr 需要转换的日期字符串
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime string2LocalDateTimeStyle3(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return LocalDate.parse(dateStr, formatter).atStartOfDay();
    }

    public static Date getTime(String timeString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_PATTERN);
        return dateFormat.parse(timeString);
    }

    /**
     * 将LocalTime转换为LocalDateTime，默认日期为当前日期
     *
     * @param localTime 需要转换的时间
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime localTime2LocalDateTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return LocalDateTime.of(LocalDate.now(), localTime);
    }

    /**
     * 将LocalTime转换为LocalDateTime，使用指定日期
     *
     * @param localTime 需要转换的时间
     * @param localDate 指定的日期
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime localTime2LocalDateTimeWithDate(LocalTime localTime, LocalDate localDate) {
        if (localTime == null || localDate == null) {
            return null;
        }
        return LocalDateTime.of(localDate, localTime);
    }

    /**
     * 根据指定的时间单位类型比较两个LocalDateTime对象是否相同
     *
     * @param type  时间单位类型，"min"表示按分钟比较，其他值表示按秒比较
     * @param time1 第一个时间对象
     * @param time2 第二个时间对象
     * @return 如果两个时间在指定精度下相同则返回true，否则返回false
     */
    public static boolean isSameByType(String type, LocalDateTime time1, LocalDateTime time2) {
        // 参数校验
        if (type == null || time1 == null || time2 == null) {
            return false;
        }

        // 定义时间单位常量
        final java.time.temporal.ChronoUnit MINUTE_UNIT = java.time.temporal.ChronoUnit.MINUTES;
        final java.time.temporal.ChronoUnit SECOND_UNIT = java.time.temporal.ChronoUnit.SECONDS;

        // 根据类型进行比较
        if ("min".equals(type)) {
            return time1.truncatedTo(MINUTE_UNIT).equals(time2.truncatedTo(MINUTE_UNIT));
        } else {
            return time1.truncatedTo(SECOND_UNIT).equals(time2.truncatedTo(SECOND_UNIT));
        }
    }

    public static String getUTCByLocal(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return "";
        }
        // 1. 将输入时间减8小时（假设输入时间为东八区时间）
        LocalDateTime adjustedTime = localDateTime.minusHours(8);
        // 2. 转换为UTC时区的时间并格式化
        return adjustedTime.atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
    }
}
