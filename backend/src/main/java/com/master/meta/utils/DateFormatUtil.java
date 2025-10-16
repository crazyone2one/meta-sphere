package com.master.meta.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Created by 11's papa on 2025/10/15
 */
public class DateFormatUtil {
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /**
     * 返回yyyy-MM-dd格式的时间
     *
     * @param localDateTime 需要转换的时间
     * @return java.lang.String
     */
    public static String localDateTime2StringStyle3(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDateTime.format(formatter);
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 将yyyy-MM-dd格式的字符串转换为LocalDateTime
     *
     * @param dateStr 需要转换的日期字符串
     * @return java.time.LocalDateTime
     */
    public static LocalDateTime string2LocalDateTimeStyle3(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateStr, formatter).atStartOfDay();
    }
}
