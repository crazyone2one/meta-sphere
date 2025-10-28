package com.master.meta.schedule;

import com.master.meta.constants.SensorMNType;
import com.master.meta.constants.SensorTypeEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/21
 */
public class CGKRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final SensorService sensorService;
    private final static String END_FLAG = "||";

    private CGKRealTimeInfo(SensorUtil sensorUtil, SensorService sensorService) {
        this.sensorUtil = sensorUtil;
        this.sensorService = sensorService;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_CGKCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();
        String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(super.projectNum).append(";").append(super.projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(super.projectNum, SensorMNType.SENSOR_SHFZ_0502, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        content.append(bodyContent(sensorList, now));
        content.append(END_FLAG);
        sensorUtil.generateFile(filePath, content.toString(), "实时数据[" + fileName + "]");
    }

    private StringBuilder bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String sensorId = row.getString("sensor_id");
            // 文件体
            content.append(sensorId).append(";")
                    .append("0").append(";");
            //水位测点值
            if (super.config.getCustomConfig().getAlarmFlag() && sensorId.equals(super.config.getCustomConfig().getSensorIds())) {
                double average = sensorService.averageForTheLastDays(sensorId, SensorTypeEnum.CGK, Duration.ofDays(7));
                content.append(RandomUtil.generateRandomDoubleString(average, average + 0.5)).append(";");
            } else {
                content.append(RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_0502)).append(";");
            }
            //水温测点值
            content.append(RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_0502)).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now));
            content.append("~");
        });
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CGKRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CGKRealTimeInfo.class.getName());
    }
}
