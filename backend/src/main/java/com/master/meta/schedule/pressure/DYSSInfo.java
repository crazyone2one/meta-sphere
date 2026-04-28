package com.master.meta.schedule.pressure;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileManager;
import com.master.meta.utils.JSON;
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
 * 地音实时数据
 *
 * @author Created by 11's papa on 2025/10/16
 */
public class DYSSInfo extends BaseScheduleJob {

    private DYSSInfo(SensorService sensorService, FileManager fileManager, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileManager, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sourceRows("dy", "sf_ky_dy");
        // 获取为删除的数据
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_DYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        // String filePath = "/app/files/ky/" + fileName;
        // sensorUtil.generateFile(filePath, content, "地音测点实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileManager.buildFilePath(slaveConfig.getLocalPath(), projectNum, "ky", fileName);
        fileManager.writeToFile(filePath, JSON.toJSONString(content), "地音测点实时数据[" + fileName + "]");
        fileManager.uploadAndCleanup(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String sensor = super.projectNum + "17" + DateFormatUtil.localDateTimeToString(localDateTime) + ";"
                    + "01;"
                    + "4500;"
                    + row.getString("point_name") + ";"
                    + DateFormatUtil.localDateTime2StringStyle2(localDateTime) + "~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DYSSInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DYSSInfo.class.getName());
    }
}
