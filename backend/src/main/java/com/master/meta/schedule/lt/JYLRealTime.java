package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
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
    private final SensorUtil sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final static String END_FLAG = "||";

    private JYLRealTime(SensorUtil sensorUtil, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
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
        List<Row> sourceRows = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.JYLDY.getKey(), WkkSensorEnum.JYLDY.getTableName(), false);
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
        String filePath = sensorUtil.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        sensorUtil.generateFile(filePath, content, WkkSensorEnum.JYLDY.getLabel() + "实时信息[" + fileName + "]");
        sensorUtil.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "wkk");
    }

    private String bodyContent(List<Row> effectiveSensor, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : effectiveSensor) {
            sb.append(sensor.getString("device_code")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append(RandomUtil.doubleTypeString(1, 5)).append(";")
                    .append("~");
        }
        return sb.toString();
    }
}
