package com.master.meta.service;

import com.influxdb.query.FluxTable;
import com.master.meta.constants.SensorTypeEnum;
import com.master.meta.utils.InfluxDbUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Service
public class SensorService {
    private final InfluxDbUtils influxDbUtils;
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public SensorService(InfluxDbUtils influxDbUtils) {
        this.influxDbUtils = influxDbUtils;
    }

    /**
     * 计算指定时间段内传感器数据的平均值
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @param time           时间间隔
     * @return 传感器在指定时间段内的平均值
     */
    public double averageForTheLastDays(String sensorId, SensorTypeEnum sensorTypeEnum, Duration time) {
        LocalDateTime lastTime = java.time.LocalDateTime.now();
        LocalDateTime startTime = lastTime.minus(time);
        String startTimeStr = getUTCByLocal(startTime);
        String endTimeStr = getUTCByLocal(lastTime);
        String query = "|> range(start: " + startTimeStr + ",stop:" + endTimeStr + ")" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"" + sensorTypeEnum.getMeasurement() + "\")" +
                " |> filter(fn: (r) => r[\"send_id\"] == \"" + sensorId + "\")" +
                " |> filter(fn: (r) => r[\"_field\"] == \"" + sensorTypeEnum.getQueryFields() + "\")" +
                " |> toFloat()" +
                " |> mean()";
        List<FluxTable> fluxTables = influxDbUtils.getData(query);
        Object value = fluxTables.getFirst().getRecords().getFirst().getValue();
        double result = (double) Optional.ofNullable(value).orElse(0.0);
        // 保留两位小数，四舍五入
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getUTCByLocal(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return "";
        }
        // 1. 将输入时间减8小时（假设输入时间为东八区时间）
        LocalDateTime adjustedTime = localDateTime.minusHours(8);
        // 2. 转换为UTC时区的时间并格式化
        return adjustedTime.atOffset(ZoneOffset.UTC).format(UTC_FORMATTER);
    }
}
