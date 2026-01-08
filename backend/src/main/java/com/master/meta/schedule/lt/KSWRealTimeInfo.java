package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 库水位实时数据
 */
public class KSWRealTimeInfo extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final FileHelper fileHelper;

    private KSWRealTimeInfo(SensorService sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.KSWDY.getKey(), WkkSensorEnum.KSWDY.getTableName());
        // 获取为删除的数据
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_" + WkkSensorEnum.KSWDY.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        fileHelper.generateFile(filePath, content, "库水位实时数据[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "wkk");
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : sensorList) {
            String deviceCode = sensor.getString("device_code");
            String sensorCode = config.getField("sensorCode", String.class);
            String sensorValue = deviceCode.equals(sensorCode)
                    ? config.getField("waterLevel", String.class)
                    : "10";
            sb.append(deviceCode).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append(sensorValue).append("~");
        }
        return sb.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, KSWRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, KSWRealTimeInfo.class.getName());
    }
}
