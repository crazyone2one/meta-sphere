package com.master.meta.schedule.pressure;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileManager;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * 顶板离层基础信息
 *
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class DBLCBasicInfo extends BaseScheduleJob {

    private DBLCBasicInfo(SensorService sensorService, FileManager fileManager, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileManager, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("DBLC", "sf_ky_dblc");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_DBLC_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String deviceType = Optional.of(config.getField("device_type", String.class)).orElse("Zkah001");
        String content = projectNum + ";" + projectName + ";顶板离层监测系统;" + deviceType + ";"
                + DateFormatUtil.localDateTime2StringStyle3(now) + ";"
                + DateFormatUtil.localDateTime2StringStyle2(now)
                + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileManager.buildFilePath(slaveConfig.getLocalPath(), projectNum, "ky", fileName);
        fileManager.writeToFile(filePath, JSON.toJSONString(content), "顶板离层信息[" + fileName + "]");
        fileManager.uploadAndCleanup(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
    }

    private String bodyContent(List<Row> parsedObject, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("sensor_area_name") + ";"
                    + row.getString("point_type") + ";"
                    + row.getString("roadway_name") + ";"
                    + row.getString("point_name") + ";"
                    + DateFormatUtil.localDateTime2StringStyle3(localDateTime) + ";"
                    + "4245615.60;36372560.60;1229.00;10;30"
                    + "~";
            content.append(sensor);
        });
        StringBuilder newSensor = new StringBuilder();
        String randomString = RandomUtil.generateRandomString(4);
        newSensor.append(sensorCode(randomString)).append(";")
                .append(randomString).append("采区-").append(randomString).append("巷道;")
                .append("1401;")
                .append(randomString).append("巷道;")
                .append(randomString).append("巷道2m;")
                .append(DateFormatUtil.localDateTime2StringStyle3(localDateTime)).append(";")
                .append(";;;;;~");
        content.append(newSensor);
        return content.toString();
    }

    private String sensorCode(String randomString) {
        return projectNum + "14MN1401" + randomString + "DBLC";
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DBLCBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DBLCBasicInfo.class.getName());
    }
}
