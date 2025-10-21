package com.master.meta.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/20
 */
@Data
public class CustomConfig {
    // 测点sensor_id
    private String sensorIds;
    // 阈值区间
    private List<Double> thresholdInterval;
    // 是否超过阈值
    private Boolean superthreshold;
    // 报警标志
    private Boolean alarmFlag;
    private String sensorType;
    private String sensorValueType;

    /**
     * 判断CustomConfig是否不为空
     *
     * @return 如果对象不为null且至少有一个字段有值则返回true，否则返回false
     */
    public boolean isNotEmpty() {
        return (sensorIds != null && !sensorIds.isEmpty()) ||
                (thresholdInterval != null && !thresholdInterval.isEmpty()) ||
                superthreshold != null ||
                alarmFlag != null || sensorType != null || sensorValueType != null;
    }
}
