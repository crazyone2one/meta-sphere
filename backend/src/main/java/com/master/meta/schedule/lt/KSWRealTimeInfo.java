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

/**
 * 库水位实时数据
 */
public class KSWRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private KSWRealTimeInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.KSWDY.getKey(), WkkSensorEnum.KSWDY.getTableName(), false);
        // 获取为删除的数据
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_" + WkkSensorEnum.KSWDY.getCdssKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = "/app/files/wkk/" + fileName;
        sensorUtil.generateFile(filePath, content, "库水位实时数据[" + fileName + "]");
        sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder sb = new StringBuilder();
        for (Row sensor : sensorList) {
            sb.append(sensor.getString("device_code")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";")
                    .append("0.1").append("~");
        }
        return sb.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, KSWRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, KSWRealTimeInfo.class.getName());
    }
}
