package com.master.meta.schedule.monitor;

import com.master.meta.constants.SensorKGType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 2025/10/22
 */
public class SmokeRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private SmokeRealTimeInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getCDSSSensorFromRedis(super.projectNum, SensorKGType.SENSOR_AQJK_1008, false);
        List<Row> sensorList = sensorInRedis.stream()
                .filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete")))
                .filter(row -> {
                    // 排除传感器类型为1003、1008和1010的数据
                    String sensorType = row.getString("sensor_type");
                    return "1008".equals(sensorType);
                })
                .toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_CDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = "/app/files/aqjk/" + fileName;
        sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
    }

    private String bodyContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        Map<String, Row> sensorMap = rows.stream()
                .collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        rows.forEach(row -> {
            String sensorValue = super.config.isYcFlag() ? "1" : "0";
            String sensorInfoCode = row.getString("sensor_code");
            Row sensor = sensorMap.get(sensorInfoCode);
            String sensorContent = sensorInfoCode + ";"
                    + sensor.getString("sensor_type_name") + ";"
                    + sensor.getString("sensor_location") + ";"
                    + sensorValue + ";"
                    + ";"
                    + "0;"
                    + DateFormatUtil.localDateTime2StringStyle2(now) + "~";
            content.append(sensorContent);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, SmokeRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, SmokeRealTimeInfo.class.getName());
    }
}
