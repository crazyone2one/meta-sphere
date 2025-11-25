package com.master.meta.utils;

import java.util.HashMap;
import java.util.Map;

public class JsonValueChecker {
    /**
     * 检查JSON对象中的值是否为空，并返回处理后的Map
     * @param jsonInput 输入的JSON字符串或Map对象
     * @return 处理后的Map，过滤掉空值
     */
    public static Map<String, Object> checkAndConvertEmptyValues(Object jsonInput) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> inputMap;

        try {
            // 如果输入是字符串，则解析为Map
            if (jsonInput instanceof String) {

                inputMap = JSON.parseObject((String) jsonInput, Map.class);
            } else if (jsonInput instanceof Map) {
                inputMap = (Map<String, Object>) jsonInput;
            } else {
                throw new IllegalArgumentException("输入必须是JSON字符串或Map对象");
            }

            // 遍历所有键值对
            for (Map.Entry<String, Object> entry : inputMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 检查值是否为空
                if (value != null && !isEmptyValue(value)) {
                    resultMap.put(key, value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMap;
    }
    private static boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            String strValue = (String) value;
            return strValue.trim().isEmpty() || "null".equals(strValue.toLowerCase());
        }

        return false;
    }
}
