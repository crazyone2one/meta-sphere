package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorKGType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class DrainageCdss extends BaseScheduleJob {

    private DrainageCdss(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_SBCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        List<Row> sensorInRedis = sourceRows(SensorKGType.SENSOR_SB_0608.getKey(), SensorKGType.SENSOR_SB_0608.getTableName());
        List<Row> unDeleted = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        StringBuilder content = new StringBuilder();
        // String filePath = "/app/files/shfz/" + fileName;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "shfz", fileName);
        // 文件头
        content.append(projectNum).append(";").append(projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        content.append(bodyContent(unDeleted, now));
        content.append(END_FLAG);
        fileHelper.generateFile(filePath, content.toString(), "水泵开停实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "shfz");
    }

    private StringBuilder bodyContent(List<Row> sensorInRedis, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String sensorCode = config.getField("sensorCode", String.class);
        if (StringUtils.isBlank(sensorCode)) {
            return content;
        }
        if (sensorInRedis.isEmpty()) {
            for (int i = 0; i < 25; i++) {
                String randomString = RandomUtil.generateRandomString(4);
                String deviceCode = projectNum + "05KG0504" + randomString;
                content.append(deviceCode).append(";");
                content.append(randomString).append("#水泵").append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append("1;");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now));
                content.append("~");
            }
        } else {
            List<Row> rows = sensorInRedis.stream().filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted"))).toList();
            rows.forEach(row -> {
                String sensorId = row.getString("sensor_id");
                content.append(sensorId).append(";");
                content.append(row.getString("pump_name")).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append(sensorId.equals(sensorCode) ? "0" : "1").append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now));
                content.append("~");
            });
        }
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DrainageCdss.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DrainageCdss.class.getName());
    }
}
