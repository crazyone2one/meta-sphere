package com.master.meta.schedule.pressure;

import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/11
 */
public class DBLCAlarm extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private static final String END_FLAG = "||";

    private DBLCAlarm(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(super.projectNum, "DBLC", "sf_ky_dblc", false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        // 生成异常文件
        String ycFileName = super.projectNum + "_LCYC_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                alarmContent(sensorList, now) + END_FLAG;
        String filePath = "/app/files/ky/" + ycFileName;
        sensorUtil.generateFile(filePath, content, "顶板离层异常数据[" + ycFileName + "]");
    }

    private String alarmContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        List<Row> rows = sensorList.stream().filter(row -> row.getString("sensor_id").equals(super.config.getCustomConfig().getSensorIds())).toList();
        Row first = rows.getFirst();
        for (int i = 0; i < 3; i++) {
            content.append(first.getString("sensor_id")).append(";")
                    .append(first.getString("point_type")).append(";")
                    .append(first.getString("point_name")).append(";")
                    .append("1;")
                    .append(config.getAdditionalFields().get("beginTime")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusSeconds(3L + i))).append(";")
                    .append("2;")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusSeconds(1L + i))).append(";")
                    .append("1;").
                    append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append("1.5;")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        }

        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DBLCAlarm.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DBLCAlarm.class.getName());
    }
}
