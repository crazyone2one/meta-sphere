package com.master.meta.service;

import com.influxdb.query.FluxTable;
import com.master.meta.constants.SensorMNType;
import com.master.meta.constants.SensorTypeEnum;
import com.master.meta.utils.InfluxDbUtils;
import com.master.meta.utils.JSON;
import com.master.meta.utils.RedisService;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Service
public class SensorService {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    private final RedisService redisService;
    private final InfluxDbUtils influxDbUtils;
    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public SensorService(RedisService redisService, InfluxDbUtils influxDbUtils) {
        this.redisService = redisService;
        this.influxDbUtils = influxDbUtils;
    }

    public List<Row> getShfzSensorList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave1");
            Map<String, Object> map = new LinkedHashMap<>();
            if (deleted) {
                map.put("deleted", "0");
            }
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getShfzSensorFromRedis(String projectNum, SensorMNType sensorMNType, Boolean deleted) {
        return getShfzSensorFromRedis(projectNum, sensorMNType.getKey(), sensorMNType.getTableName(), deleted);
    }

    public List<Row> getShfzSensorFromRedis(String projectNum, String key, String tableName, Boolean deleted) {
        String rainDefineInRedis = redisService.getSensor(projectNum, key);
        if (rainDefineInRedis != null) {
            return JSON.parseArray(rainDefineInRedis, Row.class);
        } else {
            List<Row> sensorList = getShfzSensorList(tableName, deleted);
            redisService.storeSensor(projectNum, key, sensorList, 60 * 60 * 24 * 7);
            return sensorList;
        }
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
