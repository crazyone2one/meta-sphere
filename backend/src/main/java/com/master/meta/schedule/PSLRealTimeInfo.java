package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorMNType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RandomUtil;
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
 * @author Created by 11's papa on 2025/10/22
 */
public class PSLRealTimeInfo extends BaseScheduleJob {

    private PSLRealTimeInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileCode = "_PSLCDSS_";
        String fileName = projectNum + fileCode + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();

        // 文件头
        content.append(projectNum).append(";").append(projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sourceRows(SensorMNType.SENSOR_SHFZ_PSL.getKey(), SensorMNType.SENSOR_SHFZ_PSL.getTableName());
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        content.append(bodyContent(sensorList, now));
        content.append(END_FLAG);
        // String filePath = "/app/files/shfz/" + fileName;
        // sensorUtil.generateFile(filePath, content.toString(), "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "shfz", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "PSL实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "shfz");
    }

    private StringBuilder bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String randomDoubleString = RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_PSL);
            // 文件体
            // 测点编码
            content.append(row.getString("sensor_id")).append(";");
            // 排水量
            content.append(randomDoubleString).append(";");
            // 测点状态
            content.append("0;");
            // 日累计排水量
            content.append(randomDoubleString).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now));
            content.append("~");
        });
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, PSLRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, PSLRealTimeInfo.class.getName());
    }
}
