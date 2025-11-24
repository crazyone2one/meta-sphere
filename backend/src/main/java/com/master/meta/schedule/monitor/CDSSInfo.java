package com.master.meta.schedule.monitor;

import com.master.meta.constants.SensorMNType;
import com.master.meta.dto.CustomConfig;
import com.master.meta.dto.ScheduleConfigDTO;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 2025/10/16
 */
@Slf4j
public class CDSSInfo extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final static String END_FLAG = "||";

    private CDSSInfo(SensorUtil sensorUtil) {
        this.sensorUtil = sensorUtil;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<Row> sensorInRedis = sensorUtil.getCDSSSensorFromRedis(super.projectNum, SensorMNType.SENSOR_AQJK_CO, false);
        List<Row> sensorList = sensorInRedis.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = super.projectNum + "_CDSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        String content = super.projectNum + ";" + super.projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now) +
                END_FLAG;
        String filePath = "/app/files/aqjk/" + fileName;
        sensorUtil.generateFile(filePath, content, "实时数据[" + fileName + "]");
    }

    private String bodyContent(List<Row> rows, LocalDateTime localDateTime) {
        ScheduleConfigDTO configDTO = super.config;
        CustomConfig customConfig = configDTO.getCustomConfig();
        assert customConfig != null;

        StringBuilder content = new StringBuilder();
        Map<String, Row> sensorMap = rows.stream()
                .collect(Collectors.toMap(row -> row.getString("sensor_code"), row -> row));
        val unRealInfoCode = super.config.getAdditionalFields().get("unRealInfoCode");
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
            String sensorValue;
            String sensorState = "0";
            if ("KG".equals(row.getString("sensor_value_type"))) {
                Object ftCode = config.getAdditionalFields().get("ftCode");
                if (ftCode != null && ftCode.equals(sensorInfoCode)) {
                    sensorValue = "0";
                } else {
                    sensorValue = "1";
                }
            } else {
                sensorValue = switch (sensorType) {
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
                    default -> RandomUtil.generateRandomDoubleString(configDTO.getMinValue(), configDTO.getMaxValue());
                };
            }
            if (Boolean.TRUE.equals(customConfig.getSuperthreshold())) {
                if (Objects.nonNull(customConfig.getSensorIds())) {
                    if (customConfig.getSensorIds().equals(sensorInfoCode) && "MN".equals(customConfig.getSensorValueType())) {
                        List<Double> thresholdInterval = customConfig.getThresholdInterval();
                        sensorValue = RandomUtil.generateRandomDoubleString(thresholdInterval.getFirst(), thresholdInterval.getLast());
                    }
                }
            }

            if (Objects.nonNull(customConfig.getSensorIds())) {
                if (customConfig.getSensorIds().equals(sensorInfoCode)) {
                    // 指定测点生成标校数据
                    sensorState = Optional.ofNullable(super.config.getAdditionalFields().get("sensorState").toString()).orElse("0");
                } else {
                    sensorState = "0";
                }
            }
            String sensorContent = sensorInfoCode + ";"
                    + sensor.getString("sensor_type_name") + ";"
                    + sensor.getString("sensor_location") + ";"
                    + sensorValue + ";"
                    + sensor.getString("sensor_value_unit") + ";"
                    + sensorState + ";"
                    + DateFormatUtil.localDateTime2StringStyle2(localDateTime) + "~";
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
