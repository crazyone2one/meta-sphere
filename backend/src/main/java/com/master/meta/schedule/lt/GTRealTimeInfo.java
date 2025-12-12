package com.master.meta.schedule.lt;

import com.master.meta.constants.WkkSensorEnum;
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

public class GTRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private GTRealTimeInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.GTXDY.getKey(), WkkSensorEnum.GTXDY.getTableName(), false);
        // 获取为删除的数据
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_" + WkkSensorEnum.GTXDY.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = "/app/files/wkk/" + fileName;
        sensorUtil.generateFile(filePath, content, "干滩设备实时信息[" + fileName + "]");
        // todo targetPath更改为可配置
        sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : sensorList) {
            sb.append(sensor.getString("device_code")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append("468.13").append(";")
                    .append("57.2").append(";")
                    .append("0").append("~");
        }
        return sb.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GTRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GTRealTimeInfo.class.getName());
    }
}
