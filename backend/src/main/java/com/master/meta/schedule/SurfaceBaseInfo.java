package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
public class SurfaceBaseInfo extends BaseScheduleJob {

    public SurfaceBaseInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("dbs", "sf_shfz_dbs_cddy");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_DBSCDDY_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(parsedObject) +
                END_FLAG;
        log.info("生成文件：{}", fileName);
        log.info("文件内容：{}", content);
    }

    private String bodyContent(List<Row> parsedObject) {
        StringBuilder content = new StringBuilder();
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";" + row.getString("device_type") + ";" + row.getString("install_location")
                    + ";2025-10-10;江苏中矿安华;2025-10-10;0502;m;4245615.60;36372560.60;1229.00~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, SurfaceBaseInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, SurfaceBaseInfo.class.getName());
    }
}
