package com.master.meta.schedule.monitor;

import com.master.meta.constants.SensorType;
import com.master.meta.dto.CustomConfig;
import com.master.meta.dto.ScheduleConfigDTO;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
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
 * @author Created by 11's papa on 2025/10/16
 */
@Slf4j
public class CDSSInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private CDSSInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getCDSSSensorFromRedis(super.projectNum, SensorType.CDSS, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_" + SensorType.CDSS.getKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = "/app/files/aqjk/" + fileName;
        sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
    }

    private String bodyContent(List<Row> rows, LocalDateTime localDateTime) {
        ScheduleConfigDTO configDTO = super.config;
        CustomConfig customConfig = configDTO.getCustomConfig();
        // 判断CustomConfig不为空
        boolean customFlag = customConfig != null && customConfig.isNotEmpty();
        StringBuilder content = new StringBuilder();
        Map<String, Row> sensorMap = rows.stream()
                .collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        for (Row row : rows) {
            String sensorInfoCode = row.getString("sensor_code");
            Row sensor = sensorMap.get(sensorInfoCode);
            String sensorValue;
            String sensorState = "0";
            if ("KG".equals(row.getString("sensor_value_type"))) {
                sensorValue = "1";
            } else {
                sensorValue = RandomUtil.generateRandomDoubleString(configDTO.getMinValue(), configDTO.getMaxValue());
            }
            if (customFlag) {
                if (customConfig.getSensorIds().equals(sensorInfoCode) && "MN".equals(customConfig.getSensorValueType())) {
                    if (customConfig.getThresholdInterval().getFirst() >= configDTO.getMaxValue()) {
                        sensorState = "4";
                    }
                    sensorValue = RandomUtil.generateRandomDoubleString(customConfig.getThresholdInterval().getFirst(), customConfig.getThresholdInterval().getLast());
                }
            }
            String sensorContent = sensorInfoCode + ";"
                    + sensor.getString("sensor_type_name") + ";"
                    + sensor.getString("sensor_location") + ";"
                    + sensorValue + ";"
                    + sensor.getString("sensor_value_unit") + ";"
                    + sensorState + ";"
                    + DateFormatUtil.localDateTime2StringStyle2(localDateTime) + "~";
            content.append(sensorContent);
        }
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CDSSInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CDSSInfo.class.getName());
    }
}
