package com.master.meta.schedule.monitor;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorKGType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 风筒传感器测点实时数据
 *
 * @author Created by 11's papa on 2025/10/22
 */
public class FTRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";
    private final FileHelper fileHelper;
    private final FileTransferConfiguration fileTransferConfiguration;
    LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));

    private FTRealTimeInfo(SensorUtil sensorUtil, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getCDSSSensorFromRedis(super.projectNum, SensorKGType.SENSOR_AQJK_1003, false);
        List<Row> sensorList = sensorInRedis.stream()
                .filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete")))
                .filter(row -> {
                    // 排除传感器类型为1003、1008和1010的数据
                    String sensorType = row.getString("sensor_type");
                    return SensorKGType.SENSOR_AQJK_1003.getSensorType().equals(sensorType);
                })
                .toList();
        String fileName = fileName("_CDSS_", now);
        String content = contentHeader(now) +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        // String filePath = "/app/files/aqjk/" + fileName;
        // sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
    }

    private String bodyContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        Map<String, Row> sensorMap = rows.stream()
                .collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        rows.forEach(row -> {
            String sensorValue = "1";
            String sensorInfoCode = row.getString("sensor_code");
            String sensorCode = config.getField("sensorCode", String.class);
            if (sensorInfoCode.equals(sensorCode)) {
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
        return new JobKey(resourceId, FTRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, FTRealTimeInfo.class.getName());
    }
}
