package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
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

    private DBCXRealTimeInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("DBCX", "sf_shfz_dbcx_cddy");
        List<Row> unDeleted = parsedObject.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_DBCXCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(unDeleted, now) +
                END_FLAG;
        // String filePath = "/app/files/shfz/" + fileName;
        // sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
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
