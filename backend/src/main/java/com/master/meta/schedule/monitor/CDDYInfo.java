package com.master.meta.schedule.monitor;

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

import static com.master.meta.constants.SensorMNType.SENSOR_AQJK_CO;

/**
 * @author Created by 11's papa on 2025/10/27
 */
public class CDDYInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    public CDDYInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sfAqjkSensor = getCDDYInfo();
        List<Row> unDeleteSensorList = sfAqjkSensor.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        List<Row> deleteSensorList = sfAqjkSensor.stream().filter(row -> BooleanUtils.isTrue(row.getBoolean("is_delete"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_CDDY_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" +
                "KJXXx;煤矿安全监控系统;中矿安华;2025-12-30;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(unDeleteSensorList, now) +
                END_FLAG;
        String filePath = "/app/files/aqjk/" + fileName;
        sensorUtil.generateFile(filePath, content, "基础数据[" + fileName + "]");
    }

    private String bodyContent(List<Row> unDeleteSensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String sensorInfoCode = super.projectNum + "01MN" + "01";
        String sensorIds = super.config.getCustomConfig().getSensorIds();
        for (Row s : unDeleteSensorList) {
            String sensorCode = s.getString("sensor_code");
            if (sensorIds.equals(sensorCode)) {
                continue;
            }
            content.append(sensorCode).append(";")
                    .append(s.getString("sys_code")).append(";")
                    .append(s.getString("station_code")).append(";")
                    .append(s.getString("sensor_type")).append(";")
                    .append(s.getString("sensor_value_type")).append(";")
                    .append(s.getString("sensor_value_unit")).append(";")
                    .append(s.getString("high_span")).append(";")
                    .append(s.getString("low_span")).append(";")
                    .append(s.getString("high_alarm_limit")).append(";")
                    .append(s.getString("high_unalarm_limit")).append(";")
                    .append(s.getString("low_alarm_limit")).append(";")
                    .append(s.getString("low_unalarm_limit")).append(";")
                    .append(s.getString("high_cut_limit")).append(";")
                    .append(s.getString("high_call_limit")).append(";")
                    .append(s.getString("low_cut_limit")).append(";")
                    .append(s.getString("low_call_limit")).append(";")
                    .append(s.getString("on_desc")).append(";")
                    .append(s.getString("off_desc")).append(";")
                    .append(s.getString("sensor_location")).append(";")
                    .append(s.getString("area_x")).append(";")
                    .append(s.getString("area_y")).append(";")
                    .append(s.getString("area_z")).append(";")
                    .append(s.getString("sensor_relation")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
        }
        return content.toString();
    }

    private List<Row> getCDDYInfo() {
        return sensorUtil.getCDSSSensorFromRedis(super.projectNum,SENSOR_AQJK_CO, false);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CDDYInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CDDYInfo.class.getName());
    }
}
