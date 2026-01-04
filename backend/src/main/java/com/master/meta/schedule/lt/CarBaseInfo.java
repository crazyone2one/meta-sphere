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
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class CarBaseInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final static String END_FLAG = "||";
    private final FileHelper fileHelper;

    public CarBaseInfo(SensorUtil sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.CARBASEINFO.getKey(), WkkSensorEnum.CARBASEINFO.getTableName(), false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        String fileName = projectNum + "_" + WkkSensorEnum.CARBASEINFO.getKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = "CN;卡车系统;江苏中矿安华;" + DateFormatUtil.localDateTime2StringStyle2(now) + ";5~" +
                // 文件体
                bodyContent(sensorInRedis, now) +
                END_FLAG;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "clry", fileName);
        fileHelper.generateFile(filePath, content, WkkSensorEnum.CARBASEINFO.getLabel() + "基础信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "clry");
    }

    private String bodyContent(List<Row> sensorInRedis, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        if (CollectionUtils.isNotEmpty(sensorInRedis)) {
        }
        for (int i = 0; i < 5; i++) {
            String randomString = RandomUtil.generateRandomString(12);
            content.append(projectNum).append(randomString).append(";");
            content.append(randomString).append(";");
            content.append("是;");
            content.append("39;");
            content.append("9;");
            content.append("运输;");
            content.append("最高车速150km/h等").append("~");
        }
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CarBaseInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CarBaseInfo.class.getName());
    }
}
