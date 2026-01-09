package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class LtPersonBasicInfo extends BaseScheduleJob {
    private LtPersonBasicInfo(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String fileName = projectNum + "_" + WkkSensorEnum.LTPERSON.getKey() + "_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = "DW;人员定位系统;江苏中矿安华;" + DateFormatUtil.localDateTime2StringStyle2(now) + ";10~" +
                // 文件体
                bodyContent() +
                END_FLAG;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "clry", fileName);
        fileHelper.generateFile(filePath, content, WkkSensorEnum.LTPERSON.getLabel() + "基础信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/wkk");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "clry");
    }

    private String bodyContent() {
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            String generateIdNumber = RandomUtil.generateIdNumber();
            String substring = generateIdNumber.substring(generateIdNumber.length() - 8);
            content.append(projectNum).append(substring).append(";");
            content.append(RandomUtil.generatePersonInfo(generateIdNumber)).append("~");
        }
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, LtPersonBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, LtPersonBasicInfo.class.getName());
    }
}
