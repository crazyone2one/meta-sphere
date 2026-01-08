package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author : 11's papa
 * @since : 2026/1/8, 星期四
 **/
public class QRXBasicInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final static String END_FLAG = "||";
    private final FileHelper fileHelper;

    public QRXBasicInfo(SensorUtil sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String fileName = projectNum + "_QRXDY_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        List<Row> sourceRows = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.QRX.getKey(), WkkSensorEnum.QRX.getTableName(), false);
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sourceRows, now) +
                END_FLAG;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "wkk", fileName);
        fileHelper.generateFile(filePath, content, WkkSensorEnum.QRX.getLabel() + "基础信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "wkk");
    }

    private String bodyContent(List<Row> sourceRows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        if (CollectionUtils.isNotEmpty(sourceRows)) {
            List<Row> sensorList = sourceRows.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
            sensorList.forEach(row -> {
                String sensor = row.getString("device_code") + ";"
                        + row.getString("device_name") + ";"
                        + DateFormatUtil.localDateTime2StringStyle3(now) + ";"
                        + row.getString("install_location") + ";"
                        + row.getString("manufacturer") + ";"
                        + row.getString("device_type") + ";"
                        + row.getString("power_comm_mode") + ";"
                        + row.getString("device_accuracy") + ";"
                        + row.getString("hole_depth") + ";"
                        + row.getString("infiltration_depth") + ";"
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
            String deviceCode = projectNum + "QRX" + randomString;
            String deviceName = "QRX-" + randomString;
            content.append(deviceCode).append(";").append(deviceName).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle3(now)).append(";")
                    .append(randomString).append("坝").append(";")
                    .append("zkah").append(";")
                    .append(";").append(";").append(";")
                    .append("15.00;").append("6.00;")
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

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, QRXBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, QRXBasicInfo.class.getName());
    }
}
