package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 降水量CDDY文件
 *
 * @author Created by 11's papa on 2025/10/14
 */
@Slf4j
public class RainDefineBasicInfo extends BaseScheduleJob {

    private RainDefineBasicInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }


    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> parsedObject = sourceRows("rainDefine", "sf_shfz_jsl_cddy");
        List<Row> sensorList = parsedObject.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_JSLCDDY_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList) +
                END_FLAG;
        log.info("生成文件：{}", fileName);
        log.info("文件内容：{}", content);
    }

    private String bodyContent(List<Row> parsedObject) {
        List<Row> sensorList = RandomUtil.getRandomSubList(parsedObject, 15);
        String randomAlphabetic = RandomStringUtils.insecure().nextAlphanumeric(12);
        StringBuilder content = new StringBuilder();
        String newRain = projectNum + randomAlphabetic + ";3;降雨量观测站" + randomAlphabetic + ";2025-10-10;江苏中矿安华;2025-10-10;4245615.60;36372560.60;1229.00~";
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";" + row.getString("device_type") + ";" + row.getString("install_location") + ";2025-10-10;江苏中矿安华;2025-10-10;4245615.60;36372560.60;1229.00~";
            content.append(sensor);
        });
        content.append(newRain);
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, RainDefineBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, RainDefineBasicInfo.class.getName());
    }
}
