package com.master.meta.schedule.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorKGType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主通风机传感器测点实时数据
 *
 * @author Created by 11's papa on 2025/10/22
 */
public class MainVentilatorRealTimeInfo extends BaseScheduleJob {

    private MainVentilatorRealTimeInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sourceRows(SensorKGType.SENSOR_AQJK_1010.getKey(), SensorKGType.SENSOR_AQJK_1010.getTableName());
        List<Row> sensorList = sensorInRedis.stream()
                .filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete")))
                .filter(row -> {
                    // 排除传感器类型为1003、1008和1010的数据
                    String sensorType = row.getString("sensor_type");
                    return SensorKGType.SENSOR_AQJK_1010.getSensorType().equals(sensorType);
                })
                .toList();
        Map<String, Row> sensorMap = sensorList.stream().collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = fileName("_CDSS_", now);
        String content = contentHeader(now) +
                // 文件体
                bodyContent(sensorList, now, sensorMap) +
                END_FLAG;
        // String filePath = "/app/files/aqjk/" + fileName;
        // sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
        // 异常报警文件
        if (config.isYcFlag()) {
            fileName = fileName("_YCBJ_", now);
            // filePath = "/app/files/aqjk/" + fileName;
            LocalDateTime currentDate = now.withSecond(0).withNano(0);
            StringBuilder alarmContent = new StringBuilder();
            Row sensor = sensorMap.get(this.config.getCustomConfig().getSensorIds());
            // 获取异常时间
            List<LocalDateTime> dateTimeList = JSON.objectToType(new TypeReference<List<Long>>() {
                    })
                    .apply(super.config.getAdditionalFields().get("range"))
                    .stream()
                    .map(timestamp -> LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()))
                    .toList();
            alarmContent.append(sensor.getString("sensor_code")).append(";")
                    .append(sensor.getString("sensor_type_name")).append(";")
                    .append(sensor.getString("sensor_location")).append(";")
                    .append(dateTimeList.getFirst()).append(";")
                    .append(currentDate.equals(dateTimeList.get(1)) ? dateTimeList.get(1) : "").append(";")
                    .append(";")
                    .append(";")
                    .append(";")
                    .append(";")
                    .append(";")
                    .append("abnormal reason;")
                    .append("treatment measures;")
                    .append(";")
                    .append("entered;")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
            // sensorUtil.generateFile(filePath, alarmContent.toString(), "异常报警文件数据[" + fileName + "]");
            String ycFilePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
            fileHelper.generateFile(ycFilePath, JSON.toJSONString(content), "异常报警文件数据[" + fileName + "]");
            fileHelper.uploadFile(slaveConfig, ycFilePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
        }
    }

    private String bodyContent(List<Row> rows, LocalDateTime now, Map<String, Row> sensorMap) {
        StringBuilder content = new StringBuilder();
        rows.forEach(row -> {
            String sensorValue = "1";
            String sensorInfoCode = row.getString("sensor_code");
            if (sensorInfoCode.equals(this.config.getCustomConfig().getSensorIds()) &&
                    this.config.getCustomConfig().getAlarmFlag()) {
                sensorValue = "0";
            }
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
        return new JobKey(resourceId, MainVentilatorRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, MainVentilatorRealTimeInfo.class.getName());
    }
}
