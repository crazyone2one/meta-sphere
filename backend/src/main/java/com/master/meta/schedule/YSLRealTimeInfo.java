package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorMNType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
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
 * 涌水量实时信息
 *
 * @author Created by 11's papa on 2025/10/21
 */
@Slf4j
public class YSLRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final SensorService sensorService;
    private final static String END_FLAG = "||";
    private final FileTransferConfiguration fileTransferConfiguration;
    private YSLRealTimeInfo(SensorUtil sensorUtil, SensorService sensorService, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.sensorService = sensorService;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_YSLCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();
        // String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(super.projectNum).append(";").append(super.projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sensorService.getShfzSensorFromRedis(super.projectNum, SensorMNType.SENSOR_SHFZ_YSL, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        content.append(cdssBodyContent(sensorList, now));
        content.append(END_FLAG);
        // sensorUtil.generateFile(filePath, content.toString(), "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String filePath = sensorUtil.filePath(slaveConfig.getLocalPath(), projectNum, "shfz", fileName);
        sensorUtil.generateFile(filePath, String.valueOf(content), "YSL实时信息[" + fileName + "]");
        sensorUtil.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "shfz");
    }

    private StringBuilder cdssBodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String sensorId = row.getString("sensor_id");
            // 测点编码、测点状态
            content.append(sensorId).append(";").append("0").append(";");
            //涌水量
            if (super.config.getCustomConfig().getSuperthreshold() && sensorId.equals(super.config.getCustomConfig().getSensorIds())) {
                List<Double> thresholdInterval = super.config.getCustomConfig().getThresholdInterval();
                content.append(RandomUtil.generateRandomDoubleString(thresholdInterval.getFirst(), thresholdInterval.getLast())).append(";");
            } else {
                content.append(RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_YSL)).append(";");
            }
            //时间
            content.append(DateFormatUtil.localDateTime2StringStyle2(now));
            content.append("~");
        });
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, YSLRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, YSLRealTimeInfo.class.getName());
    }
}
