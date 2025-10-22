package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/21
 */
@Getter
public enum SensorKGType {
    SENSOR_AQJK_1010("主通风机", "CDSS", "1010", "sf_aqjk_sensor", true),
    SENSOR_AQJK_1003("风筒", "CDSS", "1003", "sf_aqjk_sensor", true),
    SENSOR_AQJK_1008("烟雾", "CDSS", "1008", "sf_aqjk_sensor", true),
    ;
    private final String label;
    private final String key;
    private final String sensorType;
    private final String tableName;
    private final Boolean weatherOpen;

    SensorKGType(String label, String key, String sensorType, String tableName, Boolean weatherOpen) {
        this.key = key;
        this.label = label;
        this.sensorType = sensorType;
        this.tableName = tableName;
        this.weatherOpen = weatherOpen;
    }
}
