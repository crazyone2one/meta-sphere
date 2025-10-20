package com.master.meta.utils;

import com.mybatisflex.core.row.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author Created by 11's papa on 2025/10/15
 */
public class RandomUtil {
    public static List<Row> getRandomSubList(List<Row> originalList, int count) {
        List<Row> result = new ArrayList<>(originalList);
        Collections.shuffle(result);
        // 返回前count个元素
        return result.subList(0, Math.min(count, result.size()));
    }
    public static String doubleTypeString(int min, int max) {
        return String.format("%.2f%n", min + ((max - min) * new Random().nextDouble()));
    }
    public static String generateRandomDoubleString(double min, double max) {
        return String.format("%.2f", min + ((max - min) * new Random().nextDouble()));
    }
}
