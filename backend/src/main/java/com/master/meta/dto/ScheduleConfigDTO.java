package com.master.meta.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/17
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleConfigDTO {
    private double minValue = 0.2;
    private double maxValue = 1.5;
    private boolean ycFlag;
    // 是否是调参、标校
    private boolean tuningFlag;
    private String sensorId;
    private CustomConfig customConfig;
    // 用于存放动态字段
    private Map<String, Object> additionalFields = new HashMap<>();

    @JsonAnySetter
    public void addField(String key, Object value) {
        additionalFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public <T> T getField(String name, Class<T> type) {
        Object value = additionalFields.get(name);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        // 尝试类型转换
        return convertValue(value, type);
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return (T) value.toString();
        } else if (type == Integer.class || type == int.class) {
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
            return (T) Integer.valueOf(value.toString());
        } else if (type == Long.class || type == long.class) {
            if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
            return (T) Long.valueOf(value.toString());
        } else if (type == Boolean.class || type == boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            }
            return (T) Boolean.valueOf(value.toString());
        }

        throw new IllegalArgumentException("Cannot convert " + value.getClass() + " to " + type);
    }
}
