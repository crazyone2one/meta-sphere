package com.master.meta.schedule;

import com.master.meta.constants.SensorMNType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

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
    private final static String END_FLAG = "||";

    private YSLRealTimeInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_YSLCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();
        String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(super.projectNum).append(";").append(super.projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(super.projectNum, SensorMNType.SENSOR_SHFZ_YSL, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        content.append(cdssBodyContent(sensorList, now));
        content.append(END_FLAG);
        sensorUtil.generateFile(filePath, content.toString(), "实时数据[" + fileName + "]");
    }

    private StringBuilder cdssBodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            // 文件体
            content.append(row.getString("sensor_id")).append(";")
                    //.append(SENSOR_TYPE_YSL).append(";")
                    .append("0").append(";");
            content.append(RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_YSL.getMinValue(), SensorMNType.SENSOR_SHFZ_YSL.getMaxValue())).append(";");
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
