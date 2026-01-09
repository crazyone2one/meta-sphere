package com.master.meta.schedule.monitor;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorMNType;
import com.master.meta.dto.CustomConfig;
import com.master.meta.dto.ScheduleConfigDTO;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RandomUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 2025/10/16
 */
@Slf4j
public class CDSSInfo extends BaseScheduleJob {

    private CDSSInfo(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sourceRows(SensorMNType.SENSOR_AQJK_CO.getKey(), SensorMNType.SENSOR_AQJK_CO.getTableName());
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        //        异常开始时间
        String ycEndTimeStr = config.getAdditionalFields().get("ycEndTime").toString();
        LocalDateTime endTime = DateFormatUtil.string2LocalDateTimeStyle2(ycEndTimeStr);
        // 测点数据异常值
        String ycValue = Optional.ofNullable(config.getAdditionalFields().get("ycValue")).orElse("35").toString();
        String fileName = projectNum + "_CDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now, endTime, ycValue) +
                END_FLAG;
        // String filePath = "/app/files/aqjk/" + fileName;
        // sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
        fileHelper.generateFile(filePath, JSON.toJSONString(content), "cdss实时信息[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
        // 异常报警文件
        if (config.isYcFlag()) {
            generateYcFile(sensorList, now, ycEndTimeStr, ycValue, slaveConfig);
        }
    }

    private void generateYcFile(List<Row> sensorList, LocalDateTime now, String ycEndTime, String ycValue, FileTransferConfiguration.SlaveConfig slaveConfig) {
        LocalDateTime endTime = DateFormatUtil.string2LocalDateTimeStyle2(ycEndTime);
        Optional.ofNullable(config.getAdditionalFields().get("ycCode")).ifPresent(
                code -> {
                    List<Row> list = sensorList.stream().filter(row -> row.getString("sensor_code").equals(code)).toList();
                    Row row = list.getFirst();
                    boolean sensorValueType = "KG".equals(row.getString("sensor_value_type"));
                    String fileName = projectNum + "_YCBJ_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
                    StringBuilder content = new StringBuilder();
                    content.append(projectNum).append(";").append(projectName).append(";").append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~");
                    // 文件体
                    content.append(code).append(";");
                    content.append(row.getString("sensor_type_name")).append(";");
                    content.append(row.getString("sensor_location")).append(";");
                    content.append(row.getString("sensor_value_unit")).append(";");
                    content.append(config.getAdditionalFields().get("ycType")).append(";");
                    content.append(config.getAdditionalFields().get("ycBeginTime")).append(";");
                    content.append((now.isAfter(endTime)) ? ycEndTime : "").append(";");
                    content.append(sensorValueType ? "" : ycValue).append(";");
                    content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                    content.append(sensorValueType ? "" : ycValue).append(";");
                    content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
                    content.append(sensorValueType ? "" : ycValue).append(";");

                    content.append(";");
                    content.append(now.isAfter(endTime) ? RandomUtil.generateRandomString(8) : "").append(";");
                    content.append(";");
                    content.append(";");

                    content.append(DateFormatUtil.localDateTime2StringStyle2(endTime)).append("~");
                    content.append(END_FLAG);
                    // String filePath = "/app/files/aqjk/" + fileName;
                    // sensorUtil.generateFile(filePath, content.toString(), "异常报警数据[" + fileName + "]");
                    String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "aqjk", fileName);
                    fileHelper.generateFile(filePath, JSON.toJSONString(content), "cdss异常报警数据[" + fileName + "]");
                    fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "aqjk");
                }
        );

    }

    private String bodyContent(List<Row> rows, LocalDateTime localDateTime, LocalDateTime endTime, String ycValue) {
        ScheduleConfigDTO configDTO = config;
        CustomConfig customConfig = configDTO.getCustomConfig();
        assert customConfig != null;

        StringBuilder content = new StringBuilder();
        Map<String, Row> sensorMap = rows.stream()
                .collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        val unRealInfoCode = config.getAdditionalFields().get("unRealInfoCode");
        boolean ycFlag = config.isYcFlag() && (localDateTime.isBefore(endTime) || localDateTime.isEqual(endTime));
        Boolean ycWithoutTimeFlag = config.getField("ycWithoutTime", Boolean.class);
        String ftCode = config.getField("ftCode", String.class);
        String ftStateValue = config.getField("ftState", String.class);
        String ycCode = config.getField("ycCode", String.class);
        String sensorStateValue = config.getField("sensorState", String.class);
        AtomicReference<String> sensorState = new AtomicReference<>("0");
        for (Row row : rows) {
            String sensorInfoCode = row.getString("sensor_code");
            // 指定的测点不生成测点数据
            if (sensorInfoCode.equals(unRealInfoCode)) {
                continue;
            }
            Row sensor = sensorMap.get(sensorInfoCode);
            String sensorType = sensor.getString("sensor_type");
            // 排除对 主通风机(1010) 风筒(1003) 烟雾(1008)
            if ("1010".equals(sensorType) || "1008".equals(sensorType)) {
                continue;
            }
            AtomicReference<String> sensorValue = new AtomicReference<>("");
            // 开关量
            if ("KG".equals(row.getString("sensor_value_type"))) {
                Optional.ofNullable(ftCode).ifPresent(code -> {
                    if (code.equals(sensorInfoCode)) {
                        sensorValue.set("0");
                        sensorState.set(Optional.ofNullable(ftStateValue).orElse("1"));
                    } else {
                        sensorValue.set("1");
                    }
                });

            } else {
                // 指定测点与异常数据测点值一致
                if (ycFlag) {
                    Optional.ofNullable(ycCode).ifPresent(code -> {
                        if (code.equals(sensorInfoCode)) {
                            sensorValue.set(ycValue);
                        }
                    });
                    sensorState.set(Optional.ofNullable(sensorStateValue).orElse("0"));
                } else {
                    sensorValue.set(switch (sensorType) {
                        case "0043" -> // SENSOR_CH4
                                RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_CH4.getMinValue(), SensorMNType.SENSOR_CH4.getMaxValue());
                        case "0004" -> // SENSOR_CO
                                RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_AQJK_CO.getMinValue(), SensorMNType.SENSOR_AQJK_CO.getMaxValue());
                        case "0001" -> // SENSOR_0001
                                RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_0001.getMinValue(), SensorMNType.SENSOR_0001.getMaxValue());
                        case "0012" -> // SENSOR_0012
                                RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_0012.getMinValue(), SensorMNType.SENSOR_0012.getMaxValue());
                        case "0013" -> // SENSOR_0012
                                RandomUtil.generateRandomDoubleString(SensorMNType.SENSOR_0013.getMinValue(), SensorMNType.SENSOR_0013.getMaxValue());
                        default ->
                                RandomUtil.generateRandomDoubleString(configDTO.getMinValue(), configDTO.getMaxValue());
                    });
                }
            }


            Optional.ofNullable(ycWithoutTimeFlag).ifPresent(flag -> {
                if (BooleanUtils.isTrue(flag)) {
                    Optional.ofNullable(ycCode).ifPresent(code -> {
                        if (code.equals(sensorInfoCode)) {
                            sensorValue.set(ycValue);
                        }
                    });
                    sensorState.set(Optional.ofNullable(sensorStateValue).orElse("0"));
                }
            });

            if (Boolean.TRUE.equals(customConfig.getSuperthreshold())) {
                if (Objects.nonNull(customConfig.getSensorIds())) {
                    if (customConfig.getSensorIds().equals(sensorInfoCode) && "MN".equals(customConfig.getSensorValueType())) {
                        List<Double> thresholdInterval = customConfig.getThresholdInterval();
                        sensorValue.set(RandomUtil.generateRandomDoubleString(thresholdInterval.getFirst(), thresholdInterval.getLast()));
                    }
                }
            }
            String sensorContent = sensorInfoCode + ";"
                    + sensor.getString("sensor_type_name") + ";"
                    + sensor.getString("sensor_location") + ";"
                    + sensorValue + ";"
                    + sensor.getString("sensor_value_unit") + ";"
                    + sensorState + ";"
                    + DateFormatUtil.localDateTime2StringStyle2(localDateTime.plusSeconds(5)) + "~";
            content.append(sensorContent);
        }
        return content.toString();
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, CDSSInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, CDSSInfo.class.getName());
    }
}
