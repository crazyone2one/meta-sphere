package com.master.meta.schedule.lt;

import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 干滩设备基础信息
 *
 * @author Created by 11's papa on 2025/10/15
 */
public class GTXBasicInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private GTXBasicInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.GTXDY.getKey(), WkkSensorEnum.GTXDY.getTableName(), false);
        // 获取为删除的数据
//        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + WkkSensorEnum.GTXDY.getKey() + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorInRedis, now) +
                END_FLAG;
        String filePath = "/app/files/GNSS/" + fileName;
        sensorUtil.generateFile(filePath, content, "干滩设备基础信息[" + fileName + "]");
        // todo targetPath更改为可配置
        sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
    }

    private String bodyContent(List<Row> sensorInRedis, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        if (CollectionUtils.isNotEmpty(sensorInRedis)) {
            List<Row> sensorList = RandomUtil.getRandomSubList(sensorInRedis, 35);
            sensorList.forEach(row -> {
                String sensor = row.getString("device_code") + ";"
                        + row.getString("device_name") + ";"
                        + row.getString("install_date") + ";"
                        + row.getString("install_location") + ";"
                        + row.getString("manufacturer") + ";"
                        + row.getString("device_type") + ";"
                        + row.getString("power_comm_mode") + ";"
                        + row.getString("device_accuracy") + ";"
                        + row.getString("longitude") + ";"
                        + row.getString("latitude") + ";"
                        + row.getString("altitude") + ";"
                        + row.getString("warning_level1") + ";"
                        + row.getString("warning_level2") + ";"
                        + row.getString("warning_level3") + ";"
                        + row.getString("status") + ";"
                        + DateFormatUtil.localDateTime2StringStyle3(now) + "~";
                content.append(sensor);
            });
        } else {
            for (int i = 0; i < 15; i++) {
                String randomString = RandomUtil.generateRandomString(4);
                String deviceCode = projectNum + "GT01" + randomString;
                content.append(deviceCode).append(";");
                content.append("GT").append(randomString).append(";");
                content.append(DateFormatUtil.localDateTime2StringStyle3(now)).append(";");
                content.append(randomString).append("位置").append(";");
                content.append("jsjkah").append(";");
                content.append(";");
                content.append(";");
                content.append(";");
                content.append("107.23517236;");
                content.append("26.54516332;");
                content.append("1250.68055000;");
                content.append("100.00;");
                content.append("120.00;");
                content.append("140.00;");
                content.append("1");
                content.append("~");
            }
        }
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GTXBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GTXBasicInfo.class.getName());
    }
}
