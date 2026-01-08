package com.master.meta.schedule.lt;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.*;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class GNSSWarningInfo extends BaseScheduleJob {
    private final SensorService sensorUtil;
    private final FileHelper fileHelper;
    private final FileTransferConfiguration fileTransferConfiguration;

    private GNSSWarningInfo(SensorService sensorUtil, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorUtil = sensorUtil;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GNSSWarningInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GNSSWarningInfo.class.getName());
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sourceRows = sensorUtil.getSensorFromRedis(projectNum, WkkSensorEnum.GNSSREALRIME.getKey(), WkkSensorEnum.GNSSREALRIME.getTableName());
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> effectiveSensor = sourceRows.stream()
                .filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted")))
                .filter(s -> s.getInt("in_use") == 1)
                .filter(s -> s.getInt("breakdown") == 0)
                .toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }
        Map<String, Row> sensorMap = effectiveSensor.stream().collect(Collectors.toMap(row -> row.getString("equip_no"), row -> row));
        // earlyWarningInformation
        if (config.isYcFlag()) {
            earlyWarningInformation(sensorMap, now);
        }
    }

    /**
     * 生成GNSS预警信息文件
     *
     * @param sensorMap 传感器数据映射表，键为传感器标识，值为对应的行数据
     * @param now       当前时间
     */
    private void earlyWarningInformation(Map<String, Row> sensorMap, LocalDateTime now) {
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        // 构建预警信息文件名
        String fileName = projectNum + "_gnssalarm_"
                + DateFormatUtil.localDateTime2StringStyle2(now) + "_"
                + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        // 获取预警级别配置，如果未配置则默认为0
        // Integer alarmLevel = Optional.ofNullable(config.getField("alarmLevel", Integer.class)).orElse(0);
        Integer alarmLevel = Optional.ofNullable(config.getField("alarmLevel", Integer.class))
                .map(level -> level == 0 ? 0 : Math.min(Math.max(level, 1), 4))
                .orElse(0);
        // 处理预警时间配置
        Optional.ofNullable(config.getField("warningTime", String.class)).ifPresent(time -> {
            LocalDateTime warningTime = DateFormatUtil.string2LocalDateTimeStyle2(time);
            // 判断是否到达预警时间或预警级别为0时生成预警信息文件
            if (DateFormatUtil.isSameByType("min", warningTime, now) || alarmLevel == 0) {
                content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(warningTime));
                content.put("data", warningInformation(sensorMap, warningTime, alarmLevel));
                // String filePath = "/app/files/gnss/" + fileName;
                // sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss预警信息[" + fileName + "]");
                // sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
                String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "gnss", fileName);
                fileHelper.generateFile(filePath, JSON.toJSONString(content), "gnss实时信息[" + fileName + "]");
                // sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
                fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "GNSS");
            }
            // 1分钟后生成预警处置文件
            if (DateFormatUtil.isSameByType("min", warningTime.plusMinutes(1), now)) {
                earlyWarningDisposal(sensorMap, now, warningTime, slaveConfig);
            }
        });
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

    private Map<String, Object> warningParameters() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("horizontal_displacement", 10);
        content.put("horizontal_velocity", 1);
        content.put("horizontal_acceleration", 0.5);
        content.put("duration", 1);
        return content;
    }

    private Map<String, Object> alarmValue() {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("horizontal_displacement", 11);
        content.put("horizontal_velocity", 1.5);
        content.put("horizontal_acceleration", 1);
        content.put("duration", 1);
        return content;
    }

    /**
     * 处理早期预警信息并生成相应的文件
     *
     * @param sensorMap   传感器数据映射表，键为传感器标识，值为对应的行数据
     * @param now         当前时间
     * @param warningTime 预警时间
     */
    private void earlyWarningDisposal(Map<String, Row> sensorMap, LocalDateTime now, LocalDateTime warningTime, FileTransferConfiguration.SlaveConfig slaveConfig) {
        // 生成预警解除文件名
        String fileName = projectNum + "_clearalarm_"
                + DateFormatUtil.localDateTime2StringStyle2(now) + "_"
                + RandomUtil.generateRandomIntegerByLength(4) + ".json";

        // 构建文件内容
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("data", disposalInformation(sensorMap, now, warningTime));

        // 生成并上传预警解除文件
        // String filePath = "/app/files/gnss/" + fileName;
        // sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss预警解除信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "gnss", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "gnss实时信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "GNSS");
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
}
