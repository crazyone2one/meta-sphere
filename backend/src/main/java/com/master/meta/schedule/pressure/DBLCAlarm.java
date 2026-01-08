package com.master.meta.schedule.pressure;

import com.master.meta.config.FileTransferConfiguration;
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
 * @author Created by 11's papa on 2025/11/11
 */
public class DBLCAlarm extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private static final String END_FLAG = "||";
    private final FileHelper fileHelper;
    private final FileTransferConfiguration fileTransferConfiguration;

    private DBLCAlarm(SensorService sensorUtil, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(projectNum, "DBLC", "sf_ky_dblc");
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        // 生成异常文件

        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                alarmContent(sensorList, now) + END_FLAG;
        String ycFileName = projectNum + "_LCYC_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        // String filePath = "/app/files/ky/" + ycFileName;
        // sensorUtil.generateFile(filePath, content, "顶板离层异常数据[" + ycFileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "ky", ycFileName);
        fileHelper.generateFile(filePath, content, "信息[" + ycFileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
    }

    private String alarmContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        List<Row> rows = sensorList.stream().filter(row -> row.getString("sensor_id").equals(config.getCustomConfig().getSensorIds())).toList();
        Row first = rows.getFirst();
        for (int i = 0; i < 3; i++) {
            content.append(first.getString("sensor_id")).append(";")
                    .append(first.getString("point_type")).append(";")
                    .append(first.getString("point_name")).append(";")
                    .append("1;")
                    .append(config.getAdditionalFields().get("beginTime")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusSeconds(3L + i))).append(";")
                    .append("2;")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusSeconds(1L + i))).append(";")
                    .append("1;").
                    append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append("1.5;")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        }

        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DBLCAlarm.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DBLCAlarm.class.getName());
    }
}
