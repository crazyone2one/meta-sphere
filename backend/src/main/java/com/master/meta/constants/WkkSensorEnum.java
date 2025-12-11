package com.master.meta.constants;

import lombok.Getter;

@Getter
public enum WkkSensorEnum {
    GTXDY("干滩设备", "GTXDY", "sf_wkk_gtxdy_cddy"),
    KSWDY("库水位设备", "KSWDY", "sf_wkk_kswdy_cddy"),
    BWYDY("表面位移设备信息", "BWYDY", "sf_wkk_bwydy_cddy"),
    ;

    private final String label;
    private final String key;
    private final String tableName;

    WkkSensorEnum(String label, String key, String tableName) {
        this.label = label;
        this.key = key;
        this.tableName = tableName;
    }
}
