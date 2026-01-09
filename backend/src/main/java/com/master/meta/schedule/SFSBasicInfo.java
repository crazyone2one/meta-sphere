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
public class SFSBasicInfo extends BaseScheduleJob {

    public SFSBasicInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("sfs", "sf_shfz_sfs_cddy");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_SFSCDDY_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(parsedObject, now) +
                END_FLAG;
        log.info("生成文件：{}", fileName);
        log.info("文件内容：{}", content);
    }

    private String bodyContent(List<Row> parsedObject, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("install_location") + ";"
                    + row.getString("measurement_point_unit") + ";"
                    + row.getString("layer_target") + ";"
                    + row.getString("layer_thickness") + ";"
                    + row.getString("layer_depth") + ";"
                    + row.getString("hole_depth") + ";"
                    + row.getString("hole_diam") + ";"
                    + "4245615.60;36372560.60;1229.00;"
                    + RandomUtil.doubleTypeString(0, 2) + ";"
                    + RandomUtil.doubleTypeString(0, 50) + ";"
                    + DateFormatUtil.localDateTime2StringStyle3(now) + "~";
            content.append(sensor);
        });
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, SFSBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, SFSBasicInfo.class.getName());
    }
}
