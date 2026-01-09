package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class JYLBasicInfo extends BaseScheduleJob {

    private JYLBasicInfo(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, JYLBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, JYLBasicInfo.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sourceRows(WkkSensorEnum.JYLDY.getKey(), WkkSensorEnum.JYLDY.getTableName());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String fileName = projectNum + "_" + WkkSensorEnum.JYLDY.getKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorInRedis, now) +
                END_FLAG;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        fileHelper.generateFile(filePath, content, WkkSensorEnum.JYLDY.getLabel() + "基础信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "wkk");
    }

    private String bodyContent(List<Row> sensorInRedis, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        if (CollectionUtils.isNotEmpty(sensorInRedis)) {
            List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
            sensorList.forEach(row -> {
                String installDate = row.getString("install_date");
                List<Integer> installDateList = JSON.parseArray(installDate, Integer.class);
                LocalDate localDate = LocalDate.of(installDateList.get(0), installDateList.get(1), installDateList.get(2));
                String sensor = row.getString("device_code") + ";"
                        + row.getString("device_name") + ";"
                        + DateFormatUtil.localDate2String(localDate) + ";"
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
                        + row.getString("status") + "~";
                content.append(sensor);
            });
        }
        for (int i = 0; i < 5; i++) {
            String randomString = RandomUtil.generateRandomString(4);
            String deviceCode = projectNum + "JYL" + randomString;
            String deviceName = randomString + "雨量计";
            content.append(deviceCode).append(";").append(deviceName).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle3(now)).append(";")
                    .append(randomString).append("位置").append(";")
                    .append(randomString).append("生产厂家").append(";")
                    .append(";").append(";").append(";")
                    .append("107.46963800").append(";")
                    .append("26.38330300").append(";")
                    .append("976.39700000").append(";")
                    .append("30.00").append(";")
                    .append("20.00").append(";")
                    .append("5.00").append(";")
                    .append("1").append("~");
        }
        return content.toString();
    }
}
