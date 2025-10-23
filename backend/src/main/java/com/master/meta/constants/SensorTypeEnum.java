package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Getter
public enum SensorTypeEnum {
    CGK("ckg", "value")
    ;
    private final String measurement;
    private final String queryFields;

    SensorTypeEnum(String measurement, String queryFields) {
        this.measurement = measurement;
        this.queryFields = queryFields;
    }
}
