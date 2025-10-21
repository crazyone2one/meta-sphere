package com.master.meta.constants;

import lombok.Getter;

/**
 * @author Created by 11's papa on 2025/10/21
 */
@Getter
public enum SensorType {
    CDSS("CDSS","sf_aqjk_sensor"),
    DBLC("DBLC","sf_ky_dblc"),
    ;
    private final String key;
    private final String tableName;

    SensorType(String key, String tableName) {
        this.key = key;
        this.tableName = tableName;
    }
}
