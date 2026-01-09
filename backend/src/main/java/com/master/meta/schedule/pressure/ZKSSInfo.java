package com.master.meta.schedule.pressure;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 钻孔应力测点实时数据
 *
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class ZKSSInfo extends BaseScheduleJob {
    private ZKSSInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sourceRows( "ZKYL", "sf_ky_zkyl");
        // 获取为删除的数据
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_ZKSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        // String filePath = "/app/files/ky/" + fileName;
        // sensorUtil.generateFile(filePath, content, "钻孔应力测点实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "ky", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "钻孔应力测点实时数据[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "ky");
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("point_type") + ";"
                    + row.getString("point_name") + ";"
                    + RandomUtil.doubleTypeString(0, 30) + ";"
                    + "0;"
                    + DateFormatUtil.localDateTime2StringStyle2(localDateTime) + "~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, ZKSSInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, ZKSSInfo.class.getName());
    }
}
