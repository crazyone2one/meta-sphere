package com.master.meta.constants;

import lombok.Getter;

@Getter
public enum WkkSensorEnum {
    GTXDY("干滩设备", "GTXDY", "sf_wkk_gtxdy_cddy","GTXSS"),
    KSWDY("库水位设备", "KSWDY", "sf_wkk_kswdy_cddy","KSWSS"),
    BWYDY("表面位移设备信息", "BWYDY", "sf_wkk_bwydy_cddy","BWYSS"),
    GNSSREALRIME("GNSS实时数据", "GNSS", "sf_aqjk_gnssbaseinfo","gnssrealtime"),
    GNSSALARM("GNSS报警信息", "GNSS", "sf_aqjk_gnssalarminfo","gnssalarm"),
    ;

    private final String label;
    private final String key;
    private final String tableName;
    private final String cdssKey;

    WkkSensorEnum(String label, String key, String tableName,String cdssKey) {
        this.label = label;
        this.key = key;
        this.tableName = tableName;
        this.cdssKey = cdssKey;
    }
}
