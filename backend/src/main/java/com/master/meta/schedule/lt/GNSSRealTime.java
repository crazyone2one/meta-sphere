package com.master.meta.schedule.lt;

import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.JSON;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GNSSRealTime extends BaseScheduleJob {
    private final SensorUtil sensorUtil;

    private GNSSRealTime(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sourceRows = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.GNSSREALRIME.getKey(), WkkSensorEnum.GNSSREALRIME.getTableName(), false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> effectiveSensor = sourceRows.stream()
                .filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted")))
                .filter(s -> s.getInt("in_use") == 1)
                .filter(s -> s.getInt("breakdown") == 0)
                .toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }
        String fileName = projectNum + "_" + WkkSensorEnum.GNSSREALRIME.getCdssKey() + "_"
                + DateFormatUtil.localDateTime2StringStyle2(now) + "_"
                + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("open_pit_no", projectNum);
        content.put("data", contentData(effectiveSensor, now));
        String filePath = "/app/files/gnss/" + fileName;
        sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss实时信息[" + fileName + "]");
        sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
    }

    private List<Map<String, Object>> contentData(List<Row> sensorInRedis, LocalDateTime now) {
        List<Map<String, Object>> result = new ArrayList<>();
        sensorInRedis.forEach(s -> {
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("equip_no", s.getString("equip_no"));
            content.put("monitor_time", DateFormatUtil.localDateTime2StringStyle2(now));
            content.put("longitude", s.getString("longitude"));
            content.put("latitude", s.getString("latitude"));
            content.put("altitude", s.getString("altitude"));
            content.put("x_disp", "1.1");
            content.put("y_disp", "1.2");
            content.put("z_disp", "1.3");
            content.put("x_speed", "1.4");
            content.put("y_speed", "1.5");
            content.put("z_speed", "1.6");
            content.put("x_acc_speed", "1.7");
            content.put("y_acc_speed", "1.8");
            content.put("z_acc_speed", "1.9");
            result.add(content);
        });

        return result;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GNSSRealTime.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GNSSRealTime.class.getName());
    }
}
