package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
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

public class JYLRealTime extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final FileHelper fileHelper;

    private JYLRealTime(SensorService sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, JYLRealTime.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, JYLRealTime.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> sourceRows = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.JYLDY.getKey(), WkkSensorEnum.JYLDY.getTableName());
        List<Row> effectiveSensor = sourceRows.stream()
                .filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted"))).toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String fileName = projectNum + "_" + WkkSensorEnum.JYLDY.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(effectiveSensor, now) +
                END_FLAG;
        // String filePath = "/app/files/" + projectNum + File.separator + "wkk" + File.separator + fileName;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        fileHelper.generateFile(filePath, content, WkkSensorEnum.JYLDY.getLabel() + "实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "wkk");
    }

    private String bodyContent(List<Row> effectiveSensor, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : effectiveSensor) {
            String deviceCode = sensor.getString("device_code");
            String sensorCode = config.getField("sensorCode", String.class);
            String sensorValue = config.getField("sensorValue", String.class);
            Boolean zeroValueFlag = config.getField("zeroValue", Boolean.class);
            String zeroValue = BooleanUtils.isTrue(zeroValueFlag) ? "0" : RandomUtil.generateRandomDoubleString(0.1, 0.6);
            sb.append(deviceCode).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append(deviceCode.equals(sensorCode) ? sensorValue : zeroValue)
                    .append("~");
        }
        return sb.toString();
    }
}
