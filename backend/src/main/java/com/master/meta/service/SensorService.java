package com.master.meta.service;

import com.influxdb.query.FluxTable;
import com.master.meta.constants.SensorMNType;
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

    private static final Long TIMEOUT = 60 * 60 * 24L;

    public SensorService(RedisService redisService, InfluxDbUtils influxDbUtils) {
        this.redisService = redisService;
        this.influxDbUtils = influxDbUtils;
    }

    public List<Row> getAllDataFromSlaveDatabase(String projectNum, String tableName) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            Map<String, Object> map = new LinkedHashMap<>();
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getSensorFromRedis(String projectNum, String key, String tableName) {
        String rainDefineInRedis = redisService.getSensor(projectNum, key);
        if (rainDefineInRedis != null) {
            return JSON.parseArray(rainDefineInRedis, Row.class);
        } else {
            List<Row> sensorList = getAllDataFromSlaveDatabase(projectNum, tableName);
            redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
            return sensorList;
        }
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

    /**
     * 计算指定时间段内传感器数据的平均值
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @param time           时间间隔
     * @return 传感器在指定时间段内的平均值
     */
    public double averageForTheLastDays(String sensorId, SensorMNType sensorTypeEnum, Duration time) {
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

    /**
     * 计算指定时间段内传感器数据的总值
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @param time           时间间隔
     * @return 传感器在指定时间段内的总值
     */
    public double sumForTheLastDays(String sensorId, SensorMNType sensorTypeEnum, Duration time) {
        LocalDateTime lastTime = java.time.LocalDateTime.now();
        LocalDateTime startTime = lastTime.minus(time);
        String startTimeStr = getUTCByLocal(startTime);
        String endTimeStr = getUTCByLocal(lastTime);
        String query = "|> range(start: " + startTimeStr + ",stop:" + endTimeStr + ")" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"" + sensorTypeEnum.getMeasurement() + "\")" +
                " |> filter(fn: (r) => r[\"send_id\"] == \"" + sensorId + "\")" +
                " |> filter(fn: (r) => r[\"_field\"] == \"" + sensorTypeEnum.getQueryFields() + "\")" +
                " |> toFloat()" +
                " |> sum()";
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

    /**
     * 获取传感器最新的一条数据记录
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @return 最新的一条传感器数据记录
     */
    public Map<String, Object> getLatestSensorData(String sensorId, SensorMNType sensorTypeEnum) {
        String query = "|> range(start: -1y)" +  // 查询最近一年的数据以确保能找到记录
                " |> filter(fn: (r) => r[\"_measurement\"] == \"" + sensorTypeEnum.getMeasurement() + "\")" +
                " |> filter(fn: (r) => r[\"send_id\"] == \"" + sensorId + "\")" +
                " |> filter(fn: (r) => r[\"_field\"] == \"" + sensorTypeEnum.getQueryFields() + "\")" +
                " |> toFloat()" +
                " |> last()";  // 获取最新的记录

        List<FluxTable> fluxTables = influxDbUtils.getData(query);
        if (fluxTables.isEmpty() || fluxTables.getFirst().getRecords().isEmpty()) {
            return Collections.emptyMap();
        }

        // 提取最新记录的数据
        Map<String, Object> latestData = new HashMap<>();
        var record = fluxTables.getFirst().getRecords().getFirst();
        latestData.put("_time", record.getTime());
        latestData.put("_value", record.getValue());
        latestData.put("_field", record.getField());
        latestData.put("send_id", record.getValueByKey("send_id"));
        latestData.put("_measurement", record.getMeasurement());

        // 添加其他标签
        record.getValues().forEach((key, value) -> {
            if (!key.startsWith("_") && !"send_id".equals(key) && !"result".equals(key) && !"table".equals(key)) {
                latestData.put(key, value);
            }
        });
        return latestData;
    }

    /**
     * 获取指定时间段内的传感器数据列表
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @param startTime      开始时间
     * @param endTime        结束时间
     * @return 传感器数据列表
     */
    public List<Map<String, Object>> getSensorDataList(String sensorId, SensorMNType sensorTypeEnum,
                                                       LocalDateTime startTime, LocalDateTime endTime) {
        String startTimeStr = getUTCByLocal(startTime);
        String endTimeStr = getUTCByLocal(endTime);

        String query =
                "|> range(start: " + startTimeStr + ", stop: " + endTimeStr + ")" +
                        " |> filter(fn: (r) => r[\"_measurement\"] == \"" + sensorTypeEnum.getMeasurement() + "\")" +
                        " |> filter(fn: (r) => r[\"send_id\"] == \"" + sensorId + "\")" +
                        " |> filter(fn: (r) => r[\"_field\"] == \"" + sensorTypeEnum.getQueryFields() + "\")" +
                        " |> toFloat()" +
                        " |> sort(columns: [\"_time\"], desc: false)";

        List<FluxTable> fluxTables = influxDbUtils.getData(query);

        List<Map<String, Object>> dataList = new ArrayList<>();
        if (!fluxTables.isEmpty()) {
            for (var record : fluxTables.getFirst().getRecords()) {
                Map<String, Object> data = new HashMap<>();
                data.put("_time", record.getTime());
                data.put("_value", record.getValue());
                data.put("_field", record.getField());
                data.put("send_id", record.getValueByKey("send_id"));
                data.put("_measurement", record.getMeasurement());

                // 添加其他标签
                record.getValues().forEach((key, value) -> {
                    if (!key.startsWith("_") && !"send_id".equals(key) && !"result".equals(key) && !"table".equals(key)) {
                        data.put(key, value);
                    }
                });

                dataList.add(data);
            }
        }

        return dataList;
    }

    /**
     * 判断水位变化条件：实时数据与前30天平均值的绝对差 > 最大相邻差值*0.7 且 水位降幅>=0.1m
     *
     * @param sensorId       传感器ID
     * @param sensorTypeEnum 传感器类型枚举
     * @return 判断结果及相关数据
     */
    public Map<String, Object> checkWaterLevelVariationCondition(String sensorId, SensorMNType sensorTypeEnum) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(30);

        // 获取前30天的数据
        List<Map<String, Object>> dataList = getSensorDataList(sensorId, sensorTypeEnum, startTime, now);

        if (dataList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 计算前30天数据的平均值
        double sum = dataList.stream()
                .mapToDouble(data -> (Double) data.get("_value"))
                .sum();
        double average30Days = sum / dataList.size();

        // 获取最新数据（实时数据）
        Map<String, Object> latestData = dataList.getLast();
        double latestValue = (Double) latestData.get("_value");

        // 计算实时数据与前30天平均值的绝对差
        double absDiffWithAverage = Math.abs(latestValue - average30Days);

        // 计算相邻数据间的最大差值
        double maxAdjacentDiff = 0.0;
        for (int i = 1; i < dataList.size(); i++) {
            double currentValue = (Double) dataList.get(i).get("_value");
            double previousValue = (Double) dataList.get(i - 1).get("_value");
            double diff = Math.abs(currentValue - previousValue);
            if (diff > maxAdjacentDiff) {
                maxAdjacentDiff = diff;
            }
        }

        // 获取初始水位（30天前的第一个数据）
        double initialValue = (Double) dataList.getFirst().get("_value");

        // 计算水位降幅（初始水位 - 实时水位）
        double waterLevelDrop = initialValue - latestValue;

        // 判断条件：绝对差值 > 最大差值 * 0.7 且 水位降幅 >= 0.1m
        boolean conditionMet = absDiffWithAverage > (maxAdjacentDiff * 0.7) && waterLevelDrop >= 0.1;

        // 构造返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("conditionMet", conditionMet);
        result.put("latestValue", latestValue);
        result.put("initialValue", initialValue);
        result.put("average30Days", average30Days);
        result.put("absDiffWithAverage", absDiffWithAverage);
        result.put("maxAdjacentDiff", maxAdjacentDiff);
        result.put("threshold", maxAdjacentDiff * 0.7);
        result.put("waterLevelDrop", waterLevelDrop);
        result.put("dataCount", dataList.size());

        return result;
    }
}
