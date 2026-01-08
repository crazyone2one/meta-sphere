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
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GNSSBasicInfo extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final FileHelper fileHelper;

    private GNSSBasicInfo(SensorService sensorUtil, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.fileHelper = fileHelper;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        List<Row> sourceRows = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.GNSSBASEINFO.getKey(), WkkSensorEnum.GNSSBASEINFO.getTableName());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = projectNum + "_" + WkkSensorEnum.GNSSBASEINFO.getCdssKey() + "_"
                + DateFormatUtil.localDateTimeToString(now) + "_"
                + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("data", contentData(sourceRows, now));
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "gnss", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "GNSS设备信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "GNSS");
    }

    private List<Map<String, Object>> contentData(List<Row> sourceRows, LocalDateTime now) {
        List<Map<String, Object>> result = new ArrayList<>();
        // 获取数据库中原有的gnss数据
        sourceRows.forEach(row -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("equip_no", row.getString("equip_no"));
            map.put("equip_name", row.getString("equip_name"));
            map.put("equip_location", row.getString("equip_location"));
            map.put("open_pit_no", row.getString("open_pit_no"));
            map.put("slope_no", row.getString("slope_no"));
            map.put("installation_date", row.getString("installation_date"));
            map.put("longitude", row.getString("longitude"));
            map.put("latitude", row.getString("latitude"));
            map.put("altitude", row.getString("altitude"));
            map.put("manufacture", row.getString("manufacture"));
            map.put("equip_range", row.getString("equip_range"));
            map.put("in_use", row.getString("in_use"));
            map.put("breakdown", row.getString("breakdown"));
            map.put("create_time", DateFormatUtil.localDateTime2StringStyle2(now));
            map.put("update_time", DateFormatUtil.localDateTime2StringStyle2(now));
            result.add(map);
        });
        // 坝体边坡 04为尾矿库坝体边坡
        for (int i = 0; i < 5; i++) {
            Map<String, Object> map = new LinkedHashMap<>();
            String randomString = RandomUtil.generateRandomString(4);
            String stuff = "04" + randomString;
            map.put("equip_no", projectNum + stuff);
            map.put("equip_name", stuff);
            map.put("equip_location", stuff + "尾矿库坝体边坡");
            map.put("open_pit_no", projectNum);
            map.put("slope_no", "040" + i);
            map.put("installation_date", DateFormatUtil.localDateTime2StringStyle3(now));
            map.put("longitude", "143.52545722410633");
            map.put("latitude", "53.16101755291268");
            map.put("altitude", "5322.2");
            map.put("manufacture", "江苏中矿安华");
            map.put("equip_range", "1000");
            map.put("in_use", "1");
            map.put("breakdown", "0");
            map.put("create_time", DateFormatUtil.localDateTime2StringStyle2(now));
            map.put("update_time", DateFormatUtil.localDateTime2StringStyle2(now));
            result.add(map);
        }
        // 采场边坡 01 为采场边坡
        for (int i = 0; i < 5; i++) {
            Map<String, Object> map = new LinkedHashMap<>();
            String randomString = RandomUtil.generateRandomString(4);
            String stuff = "01" + randomString;
            map.put("equip_no", projectNum + stuff);
            map.put("equip_name", stuff);
            map.put("equip_location", stuff + "采场边坡");
            map.put("open_pit_no", projectNum);
            map.put("slope_no", "010" + i);
            map.put("installation_date", DateFormatUtil.localDateTime2StringStyle3(now));
            map.put("longitude", "143.52545722410633");
            map.put("latitude", "53.16101755291268");
            map.put("altitude", "5322.2");
            map.put("manufacture", "江苏中矿安华");
            map.put("equip_range", "1000");
            map.put("in_use", "1");
            map.put("breakdown", "0");
            map.put("create_time", DateFormatUtil.localDateTime2StringStyle2(now));
            map.put("update_time", DateFormatUtil.localDateTime2StringStyle2(now));
            result.add(map);
        }
        return result;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GNSSBasicInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GNSSBasicInfo.class.getName());
    }
}
