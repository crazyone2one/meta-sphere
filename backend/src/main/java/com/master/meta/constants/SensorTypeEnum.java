package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/27
 */
@Getter
public enum SensorTypeEnum {
    CGK("cgk", "cgk_value");
    private final String measurement;
    private final String queryFields;

    SensorTypeEnum(String measurement, String queryFields) {
        this.measurement = measurement;
        this.queryFields = queryFields;
    }
}
