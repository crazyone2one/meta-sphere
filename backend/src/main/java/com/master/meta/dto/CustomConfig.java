package com.master.meta.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/20
 */
@Data
public class CustomConfig {
    // 测点sensor_id
    private List<String> sensorIds;
    // 阈值区间
    private List<Double> thresholdInterval;
    // 超阈值
    private Boolean superthreshold;
    // 报警标志
    private Boolean alarmFlag;
}
