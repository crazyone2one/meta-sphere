package com.master.meta.schedule;

import com.master.meta.constants.SensorKGType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class DrainageCdss extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private DrainageCdss(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_SBCDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        List<Row> sensorInRedis = sensorUtil.getCDSSSensorFromRedis(projectNum, SensorKGType.SENSOR_SB_0608, false);
        StringBuilder content = new StringBuilder();
        String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(projectNum).append(";").append(projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        content.append(bodyContent(sensorInRedis, now));
        content.append(END_FLAG);
        sensorUtil.generateFile(filePath, content.toString(), "水泵开停实时信息[" + fileName + "]");
    }

    private StringBuilder bodyContent(List<Row> sensorInRedis, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
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
                content.append(row.getString("sensor_id")).append(";");
                content.append(row.getString("pump_name")).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                content.append("1;");
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
