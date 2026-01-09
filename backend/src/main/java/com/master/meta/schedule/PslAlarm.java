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
import java.util.Random;

/**
 * @author Created by 11's papa on 2025/11/11
 */
public class PslAlarm extends BaseScheduleJob {

    Random random = new Random();

    private PslAlarm(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_PSLCDYC_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();
        // String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(projectNum).append(";").append(projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sourceRows(SensorMNType.SENSOR_SHFZ_PSL.getKey(), SensorMNType.SENSOR_SHFZ_PSL.getTableName());
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        List<Row> rows = sensorList.stream().filter(row -> row.getString("sensor_id").equals(config.getCustomConfig().getSensorIds())).toList();
        content.append(ycBodyContent(rows.getFirst(), now));
        content.append(END_FLAG);
        // sensorUtil.generateFile(filePath, content.toString(), "排水量异常数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "shfz", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "排水量异常[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "shfz");
    }

    private StringBuilder ycBodyContent(Row row, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            // 测点编码
            content.append(row.getString("sensor_id")).append(";");
            content.append(row.getString("install_location")).append(";");
            content.append("1").append(";");
            content.append("排水量异常内容：").append(RandomUtil.generateRandomString(20)).append(";");
            // 发生时间
            content.append(config.getAdditionalFields().get("beginTime")).append(";");
            content.append(random.nextInt(5)).append(";");
            content.append("排水量异常原因：").append(RandomUtil.generateRandomString(20)).append(";");
            content.append(RandomUtil.generateRandomString(10)).append(";");
            // 异常结束时间
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        }
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, PslAlarm.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, PslAlarm.class.getName());
    }
}
