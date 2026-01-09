package com.master.meta.schedule.pressure;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
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

/**
 * 顶板离层基础信息
 *
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class DBLCBasicInfo extends BaseScheduleJob {

    private DBLCBasicInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("DBLC", "sf_ky_dblc");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_DBLC_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";顶板离层监测系统;KJ001;"
                + DateFormatUtil.localDateTime2StringStyle3(now) + ";"
                + DateFormatUtil.localDateTime2StringStyle2(now)
                + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "ky", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "顶板离层信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
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
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DBLCBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DBLCBasicInfo.class.getName());
    }
}
