package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/27
 */
public class RYJZInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final FileHelper fileHelper;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final static String END_FLAG = "||";

    public RYJZInfo(SensorUtil sensorUtil, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sfAqjkSensor = getStationInfo();
        List<Row> unDeleteSensorList = sfAqjkSensor.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_RYJZ_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" +"300;"+
                "KJXXx;中矿安华;01x;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(unDeleteSensorList, now) +
                END_FLAG;
        // String filePath = "/app/files/aqjk/" + fileName;
        // sensorUtil.generateFile(filePath, content, "基础数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "基础数据[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
    }

    private String bodyContent(List<Row> unDeleteSensorList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String sensorIds = super.config.getCustomConfig().getSensorIds();
        for (Row s : unDeleteSensorList) {
            String sensorCode = s.getString("station_code");
            if (sensorIds.equals(sensorCode)) {
                continue;
            }
            content.append(sensorCode).append(";")
                    .append("20;")
                    .append(s.getString("area_x")).append(";")
                    .append(s.getString("area_y")).append(";")
                    .append("4424264.6320;")
                    .append(s.getString("area_desc")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
        }
        return content.toString();
    }

    private List<Row> getStationInfo() {
        return sensorUtil.getCDSSList("sf_jxzy_substation", false);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, RYJZInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, RYJZInfo.class.getName());
    }
}
