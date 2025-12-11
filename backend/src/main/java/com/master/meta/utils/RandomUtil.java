package com.master.meta.utils;

import com.master.meta.constants.SensorMNType;
import com.mybatisflex.core.row.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Created by 11's papa on 2025/10/15
 */
public class RandomUtil {
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random random = new Random();

    public static String generateRandomString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(index));
        }

        return builder.toString();
    }

    public static List<Row> getRandomSubList(List<Row> originalList, int count) {
        List<Row> result = new ArrayList<>(originalList);
        Collections.shuffle(result);
        // 返回前count个元素
        return result.subList(0, Math.min(count, result.size()));
    }

    /**
     * 生成指定范围内的随机双精度浮点数字符串，保留两位小数
     *
     * @param min 随机数范围的最小整数值
     * @param max 随机数范围的最大整数值
     * @return 格式化后的随机双精度浮点数字符串，保留两位小数并换行
     */
    public static String doubleTypeString(int min, int max) {
        return String.format("%.2f%n", min + ((max - min) * random.nextDouble()));
    }

    /**
     * 生成指定范围内的随机双精度浮点数字符串
     *
     * @param min 随机数的最小值
     * @param max 随机数的最大值
     * @return 格式化为两位小数的随机双精度浮点数字符串
     */
    public static String generateRandomDoubleString(double min, double max) {
        return String.format("%.2f", min + ((max - min) * random.nextDouble()));
    }

    /**
     * 生成指定传感器类型范围内的随机双精度浮点数字符串
     *
     * @param sensorMNType 传感器类型，用于确定随机数的最小值和最大值范围
     * @return 在传感器类型最小值和最大值之间的随机双精度浮点数字符串，保留两位小数
     */
    public static String generateRandomDoubleString(SensorMNType sensorMNType) {
        return String.format("%.2f", sensorMNType.getMinValue() + ((sensorMNType.getMaxValue() - sensorMNType.getMinValue()) * new Random().nextDouble()));
    }
}
