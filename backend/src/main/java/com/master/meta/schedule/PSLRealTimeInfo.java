package com.master.meta.schedule;

import com.master.meta.constants.SensorMNType;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/22
 */
public class PSLRealTimeInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private PSLRealTimeInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileCode = super.config.isYcFlag() ? "_PSLCDYC_" : "_PSLCDSS_";
        String fileName = super.projectNum + fileCode + DateFormatUtil.localDateTimeToString(now) + ".txt";
        StringBuilder content = new StringBuilder();
        String filePath = "/app/files/shfz/" + fileName;
        // 文件头
        content.append(super.projectNum).append(";").append(super.projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
        // 文件体
        List<Row> sensorInRedis = sensorUtil.getSensorFromRedis(super.projectNum, SensorMNType.SENSOR_SHFZ_PSL, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        if (super.config.isYcFlag()) {
            List<Row> rows = sensorList.stream().filter(row -> row.getString("sensor_id").equals(super.config.getSensorId())).toList();
            content.append(ycBodyContent(rows.getFirst(), now));
        } else {
            content.append(bodyContent(sensorList, now));
        }
        content.append(END_FLAG);
        sensorUtil.generateFile(filePath, content.toString(), "实时数据[" + fileName + "]");
    }

    private StringBuilder ycBodyContent(Row row, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        // 测点编码
        content.append(row.getString("sensor_id")).append(";");
        content.append(row.getString("install_location")).append(";");
        content.append("1").append(";");
        content.append("排水量异常内容：").append(RandomUtil.generateRandomString(20)).append(";");
        // 发生时间
        content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
        content.append("4").append(";");
        content.append("排水量异常原因：").append(RandomUtil.generateRandomString(20)).append(";");
        content.append(RandomUtil.generateRandomString(10)).append(";");
        // 异常结束时间
        content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
        content.append(DateFormatUtil.localDateTime2StringStyle2(now));
        return content;
    }

    private StringBuilder bodyContent(List<Row> sensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String randomDoubleString = RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_SHFZ_PSL);
            // 文件体
            // 测点编码
            content.append(row.getString("sensor_id")).append(";");
            // 排水量
            content.append(randomDoubleString).append(";");
            // 测点状态
            content.append("0;");
            // 日累计排水量
            content.append(randomDoubleString).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now));
            content.append("~");
        });
        return content;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, PSLRealTimeInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, PSLRealTimeInfo.class.getName());
    }
}
