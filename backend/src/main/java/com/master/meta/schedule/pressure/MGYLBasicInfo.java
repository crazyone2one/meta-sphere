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
 * 锚杆(索)应力基础信息
 *
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class MGYLBasicInfo extends BaseScheduleJob {

    private MGYLBasicInfo(SensorService sensorService, FileManager fileManager, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileManager, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("MGYL", "sf_ky_mgsyl");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_MGYL_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String deviceType = Optional.of(config.getField("device_type", String.class)).orElse("device" + RandomUtil.generateRandomString(3));
        String content = super.projectNum + ";" + super.projectName + ";锚杆(索)应力监测系统;" + deviceType + ";"
                + DateFormatUtil.localDateTime2StringStyle3(now) + ";"
                + DateFormatUtil.localDateTime2StringStyle2(now)
                + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileManager.buildFilePath(slaveConfig.getLocalPath(), projectNum, "ky", fileName);
        fileManager.writeToFile(filePath, JSON.toJSONString(content), "锚杆(索)应力信息[" + fileName + "]");
        fileManager.uploadAndCleanup(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
    }

    private String bodyContent(List<Row> parsedObject, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("sensor_area_name") + ";"
                    + row.getString("point_type") + ";"
                    + row.getString("point_name") + ";"
                    + DateFormatUtil.localDateTime2StringStyle3(localDateTime) + ";"
                    + "4245615.60;36372560.60;1229.00;100"
                    + "~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, MGYLBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, MGYLBasicInfo.class.getName());
    }
}
