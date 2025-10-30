package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/21
 */
@Getter
public enum SensorMNType {
    SENSOR_AQJK_CO("一氧化碳", "CDSS", "0004", "sf_aqjk_sensor", 0.1, 0.5, "", ""),
    SENSOR_0013("二氧化碳", "CDSS", "0013", "sf_aqjk_sensor", 0.1, 0.5, "", ""),
    SENSOR_CH4("激光甲烷", "CDSS", "0043", "sf_aqjk_sensor", 0.1, 0.5, "", ""),
    SENSOR_0001("环境瓦斯", "CDSS", "0001", "sf_aqjk_sensor", 0.1, 0.5, "", ""),
    SENSOR_0012("氧气", "CDSS", "0012", "sf_aqjk_sensor", 0.1, 0.5, "", ""),
    SENSOR_SHFZ_YSL("涌水量", "ysl", "0503", "sf_shfz_ysl_cddy", 0.1, 0.5, "sf_shfz_ysl_cdss", "numerical_value"),
    SENSOR_SHFZ_0502("长观孔", "CGK", "0502", "sf_shfz_cgk_cddy", 0.1, 0.5, "sf_shfz_cgk_cdss", "water_level_value"),
    SENSOR_SHFZ_PSL("排水量", "psl", "0502", "sf_shfz_psl_cddy", 0.1, 0.5, "", ""),
    ;
    private final String label;
    private final String key;
    private final String sensorType;
    private final String tableName;
    private final Double minValue;
    private final Double maxValue;
    private final String measurement;
    private final String queryFields;

    SensorMNType(String label, String key, String sensorType, String tableName, Double minValue, Double maxValue, String measurement, String queryFields) {
        this.key = key;
        this.label = label;
        this.sensorType = sensorType;
        this.tableName = tableName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.measurement = measurement;
        this.queryFields = queryFields;
    }
}
