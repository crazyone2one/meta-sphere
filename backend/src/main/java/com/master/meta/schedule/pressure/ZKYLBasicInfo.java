package com.master.meta.schedule.pressure;

import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 钻孔应力测点基本信息
 *
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class ZKYLBasicInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private ZKYLBasicInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sensorUtil.getSensorFromRedis(super.projectNum, "ZKYL", "sf_ky_zkyl", false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_ZKYL_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        log.info("生成文件：{}", fileName);
        log.info("文件内容：{}", content);
    }

    private String bodyContent(List<Row> parsedObject, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("sensor_area_name") + ";"
                    + row.getString("point_type") + ";"
                    + row.getString("point_name") + ";"
                    + row.getString("install_depth") + ";"
                    + DateFormatUtil.localDateTime2StringStyle3(localDateTime) + ";"
                    + "4245615.60;36372560.60;0;X;12&10&100"
                    + "~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ZKYLBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ZKYLBasicInfo.class.getName());
    }
}
