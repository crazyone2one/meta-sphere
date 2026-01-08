package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * QRX实时信息
 */
public class QRXRealTime extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final FileHelper fileHelper;

    private QRXRealTime(SensorService sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, QRXRealTime.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, QRXRealTime.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> sourceRows = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.QRX.getKey(), WkkSensorEnum.QRX.getTableName());
        List<Row> effectiveSensor = sourceRows.stream()
                .filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted"))).toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String fileName = projectNum + "_" + WkkSensorEnum.QRX.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(effectiveSensor, now) +
                END_FLAG;
        // String filePath = "/app/files/" + projectNum + File.separator + "wkk" + File.separator + fileName;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        fileHelper.generateFile(filePath, content, "QRX实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "wkk");
    }

    private String bodyContent(List<Row> effectiveSensor, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : effectiveSensor) {
            String deviceCode = sensor.getString("device_code");
            Double infiltrationDepth = sensor.getDouble("infiltration_depth");
            String sensorCode = config.getField("sensorCode", String.class);
            String valueLevel = config.getField("valueLevel", String.class);
            String sensorValue = deviceCode.equals(sensorCode)
                    ? String.valueOf(sensorValueByLevel(infiltrationDepth, valueLevel))
                    : String.valueOf(infiltrationDepth + 1);
            sb.append(deviceCode).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusSeconds(10))).append(";")
                    .append(sensorValue).append(";")
                    .append("0").append("~");
        }
        return sb.toString();
    }

    private double sensorValueByLevel(Double infiltrationDepth, String valueLevel) {
        double result = switch (valueLevel) {
            // 红
            case "1" -> infiltrationDepth * 0.8 - 0.02;
            // 橙
            case "2" -> infiltrationDepth * 0.85;
            // 黄
            case "3" -> infiltrationDepth * 0.9;
            // 蓝
            case "4" -> infiltrationDepth * 0.9 + 0.1;
            default -> infiltrationDepth;
        };
        // 保留两位小数
        return Math.round(result * 100.0) / 100.0;
    }
}
