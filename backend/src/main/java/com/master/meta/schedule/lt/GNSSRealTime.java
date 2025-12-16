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
import java.util.*;
import java.util.stream.Collectors;

public class GNSSRealTime extends BaseScheduleJob {
    private final SensorUtil sensorUtil;

    private GNSSRealTime(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sourceRows = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.GNSSREALRIME.getKey(), WkkSensorEnum.GNSSREALRIME.getTableName(), false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> effectiveSensor = sourceRows.stream().filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted"))).toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }
        String fileName = projectNum + "_" + WkkSensorEnum.GNSSREALRIME.getCdssKey() + "_" + DateFormatUtil.localDateTime2StringStyle2(now)
                + RandomUtil.generateRandomIntegerByLength(4)
                + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("open_pit_no", projectNum);
        content.put("data", contentData(effectiveSensor, now));
        String filePath = "/app/files/gnss/" + fileName;
        sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss实时信息[" + fileName + "]");
        sensorUtil.uploadFile(filePath, "/home/app/ftp/gnss");
        Map<String, Row> sensorMap = effectiveSensor.stream().collect(Collectors.toMap(row -> row.getString("equip_no"), row -> row));
        // earlyWarningInformation
        if (config.isYcFlag()) {
            earlyWarningInformation(sensorMap, now);
        }
    }

    /**
     * 预警信息
     *
     * @param sensorMap
     * @param now
     */
    private void earlyWarningInformation(Map<String, Row> sensorMap, LocalDateTime now) {
        String fileName = projectNum + "_gnssalarm_" + DateFormatUtil.localDateTime2StringStyle2(now) + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        Integer alarmLevel = Optional.ofNullable(config.getField("alarmLevel", Integer.class)).orElse(0);

        Optional.ofNullable(config.getField("warningTime", String.class)).ifPresent(time -> {
            LocalDateTime warningTime = DateFormatUtil.string2LocalDateTimeStyle2(time);
            if (DateFormatUtil.isSameByType("min", warningTime, now) || alarmLevel == 0) {
                content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(warningTime));
                content.put("data", warningInformation(sensorMap, warningTime, alarmLevel));
                String filePath = "/app/files/gnss/" + fileName;
                sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss预警信息[" + fileName + "]");
                sensorUtil.uploadFile(filePath, "/home/app/ftp/gnss");
            }
            if (alarmLevel == 0) {
                earlyWarningDisposal(sensorMap, now, warningTime);
            }
        });
    }

    /**
     * 预警处置处理
     *
     * @param sensorMap
     * @param now
     */
    private void earlyWarningDisposal(Map<String, Row> sensorMap, LocalDateTime now, LocalDateTime warningTime) {
        String fileName = projectNum + "_clearalarm_" + DateFormatUtil.localDateTime2StringStyle2(now) + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("data", disposalInformation(sensorMap, now, warningTime));
        String filePath = "/app/files/gnss/" + fileName;
        sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss预警解除信息[" + fileName + "]");
        sensorUtil.uploadFile(filePath, "/home/app/ftp/gnss");
    }

    private List<Map<String, Object>> disposalInformation(Map<String, Row> sensorMap, LocalDateTime now, LocalDateTime warningTime) {
        List<Map<String, Object>> result = new ArrayList<>();
        Optional.ofNullable(config.getAdditionalFields().get("warningCode"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(sensorMap::get)
                .ifPresent(row -> {
                    Map<String, Object> content = new LinkedHashMap<>();
                    content.put("equip_no", row.getString("equip_no"));
                    content.put("alarm_time", DateFormatUtil.localDateTime2StringStyle2(warningTime));
                    content.put("handle_time", DateFormatUtil.localDateTime2StringStyle2(now));
                    content.put("description", "本次预警原因为" + RandomUtil.generateRandomString(10) + "，经综合研判风险较大，已安排人员撤离，并采取削坡方案");
                    result.add(content);
                });
        return result;
    }

    private List<Map<String, Object>> warningInformation(Map<String, Row> sensorMap, LocalDateTime now, Integer alarmLevel) {
        List<Map<String, Object>> result = new ArrayList<>();
        Optional.ofNullable(config.getAdditionalFields().get("warningCode"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(sensorMap::get)
                .ifPresent(row -> {
                    Map<String, Object> content = new LinkedHashMap<>();
                    content.put("equip_no", row.getString("equip_no"));
                    content.put("alarm_time", DateFormatUtil.localDateTime2StringStyle2(now));
                    content.put("alarm_level", alarmLevel);
                    content.put("warning_parameters", warningParameters());
                    content.put("alarm_value", alarmValue());
                    result.add(content);
                });
        return result;
    }

    /**
     * 预警时的监测值
     */
    private Map<String, Object> alarmValue() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("horizontal_displacement", 11);
        content.put("horizontal_velocity", 1.5);
        content.put("horizontal_acceleration", 1);
        content.put("duration", 1);
        return content;
    }

    /**
     * 预警阈值
     *
     * @return 水平方向位移、速度、加速度
     */
    private Map<String, Object> warningParameters() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("horizontal_displacement", 10);
        content.put("horizontal_velocity", 1);
        content.put("horizontal_acceleration", 0.5);
        content.put("duration", 1);
        return content;
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
