package com.master.meta;

import com.master.meta.constants.SensorMNType;
import com.master.meta.service.SensorService;
import com.master.meta.utils.InfluxDbUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 2025/11/3
 */
@Slf4j
@SpringBootTest
class AQJKYJ006 {
    @Resource
    InfluxDbUtils influxDbUtils;
    @Resource
    SensorService sensorService;

    @Test
    void testRemoveInfluxData() {
        String measurement = "sf_shfz_cgk_cdss";
        String sensorId = "150622004499MNAEDKamaMj";
        OffsetDateTime startTime = OffsetDateTime.parse("2025-10-21T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2025-11-01T00:00:00Z");
        influxDbUtils.deleteDataByTimeRange(measurement, sensorId, startTime, endTime);
    }

    @Test
    void testUpdateInfluxData() {
        String measurement = "sf_shfz_cgk_cdss";
        String sensorId = "150622004499MNAEDKamaMj";
        Map<String, Object> fields = new HashMap<>();
        fields.put("water_level_value", 0.19);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse("2025-10-21T09:10:00Z");
        influxDbUtils.updateData(measurement, sensorId, fields, offsetDateTime);
    }

    @Test
    void test() {
        String sensorId = "150622004499MNAEDKamaMj";
        calculatedData(sensorId, 0.7);
//        calculatedData(sensorId, 0.9);
//        calculatedData(sensorId, 1.5);
//        calculatedData(sensorId, 2.0);
    }

    private void calculatedData(String sensorId, double cardinal) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(30);
        // 近30天数据总量
        double sum = sensorService.sumForTheLastDays(sensorId, SensorMNType.SENSOR_SHFZ_0502, Duration.ofDays(30));
        List<Map<String, Object>> sensorDataList = sensorService.getSensorDataList(sensorId, SensorMNType.SENSOR_SHFZ_0502, startTime, now);

        double average30Days = sum / sensorDataList.size();
        // 初始水位
       double first = (double) sensorDataList.getFirst().get("_value");
        // double first = 2.5;
        // 获取近30天最大差值
        double maxBetweenAdjacentData = getMaxDifferenceBetweenAdjacentData(sensorDataList);
        // 获取最新数据（实时数据）
        Map<String, Object> latestData = sensorDataList.getLast();
//        double latestValue = (Double) latestData.get("_value");
        double latestValue = 0.1;
        // 计算实时数据与前30天平均值的绝对差
        double absDiffWithAverage = Math.abs(latestValue - average30Days);
        log.info("初始水位{}==》实时数据{}==》水位降幅{}", first, latestValue, first - latestValue);
        log.info("绝对值【{}】与最大差值【{}】*{}=>{}", absDiffWithAverage, maxBetweenAdjacentData, cardinal, absDiffWithAverage - maxBetweenAdjacentData * cardinal);
    }

    public double getMaxDifferenceBetweenAdjacentData(List<Map<String, Object>> sensorDataList) {
        if (sensorDataList == null || sensorDataList.size() < 2) {
            return 0.0;
        }

        double maxDifference = 0.0;
        for (int i = 1; i < sensorDataList.size(); i++) {
            Object currentValueObj = sensorDataList.get(i).get("_value");
            Object previousValueObj = sensorDataList.get(i - 1).get("_value");

            // 确保两个值都是数字类型
            if (currentValueObj instanceof Number && previousValueObj instanceof Number) {
                double currentValue = ((Number) currentValueObj).doubleValue();
                double previousValue = ((Number) previousValueObj).doubleValue();
                double difference = Math.abs(currentValue - previousValue);

                if (difference > maxDifference) {
                    maxDifference = difference;
                }
            }
        }

        return maxDifference;
    }
}
