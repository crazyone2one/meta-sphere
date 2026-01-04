package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 地表岩移实时数据
 */
@Slf4j
public class DBCXRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";
    private final FileHelper fileHelper;
    private final FileTransferConfiguration fileTransferConfiguration;

    private DBCXRealTimeInfo(SensorUtil sensorUtil, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sensorUtil.getSensorFromRedis(super.projectNum, "DBCX", "sf_shfz_dbcx_cddy", false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_DBCXCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        // String filePath = "/app/files/shfz/" + fileName;
        // sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "shfz", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "DBCX实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "shfz");
    }

    private String bodyContent(List<Row> parsedObject, LocalDateTime now) {
        List<Row> sensors = parsedObject.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        StringBuilder content = new StringBuilder();
        sensors.forEach(row -> {
            content.append(row.getString("sensor_id")).append(";");
            content.append(row.getString("area_name")).append(";");
            content.append(row.getString("point_id")).append(";");
            content.append("0;");
            content.append("1.171;1.971;1.971;");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now));
            content.append("~");
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DBCXRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DBCXRealTimeInfo.class.getName());
    }
}
