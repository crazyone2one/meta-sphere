package com.master.meta;

import com.master.meta.constants.SensorMNType;
import com.master.meta.service.SensorService;
import com.master.meta.utils.InfluxDbUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author : 11's papa
 * @since : 2026/1/4, 星期日
 **/
@Slf4j
@SpringBootTest
public class AQJKYJ007Test {
    @Resource
    SensorService sensorService;
    @Resource
    InfluxDbUtils influxDbUtils;

    @Test
    void getLastSevenDays() {
        LocalDateTime now = LocalDateTime.now().minusSeconds(10);
        LocalDateTime startTime = now.minusDays(7);

        List<Map<String, Object>> sensorDataList = sensorService.getSensorDataList("150622004499YSLAUlwHpdOB", SensorMNType.SENSOR_SHFZ_YSL, startTime, now);
        // 计算近7天涌水量的平均值
        double sum = sensorDataList.stream().mapToDouble(data -> (double) data.get("_value")).sum();
        // double avgOf7Days = sum / sensorDataList.size();
        double avgOf7Days = sum / 7;
        System.out.println("近7天涌水量的平均值：" + avgOf7Days);

        LocalDateTime startTimeFor1Day = now.minusDays(1);
        List<Map<String, Object>> sensorDataListFor1Day = sensorService.getSensorDataList("150622004499YSLAUlwHpdOB", SensorMNType.SENSOR_SHFZ_YSL, startTimeFor1Day, now);
        double sumFor1Day = sensorDataListFor1Day.stream().mapToDouble(data -> (double) data.get("_value")).sum();
        System.out.println("近1天涌水量：" + sumFor1Day);
        System.out.println("x{" + sumFor1Day + "}>y{" + avgOf7Days + "}*130%{" + avgOf7Days * 1.3 + "}==> " + (sumFor1Day - avgOf7Days * 1.3));
        System.out.println("x{" + sumFor1Day + "}>y{" + avgOf7Days + "}*150%{" + avgOf7Days * 1.5 + "}==> " + (sumFor1Day - avgOf7Days * 1.5));
        System.out.println("x{" + sumFor1Day + "}>y{" + avgOf7Days + "}*180%{" + avgOf7Days * 1.8 + "}==> " + (sumFor1Day - avgOf7Days * 1.8));
        System.out.println("x{" + sumFor1Day + "}>y{" + avgOf7Days + "}*200%{" + avgOf7Days * 2 + "}==> " + (sumFor1Day - avgOf7Days * 2));
    }


    @Test
    void removeData() {
        String measurement = "sf_shfz_ysl_cdss";
        String sensorId = "150622004499YSLAUlwHpdOB";
        OffsetDateTime startTime = OffsetDateTime.parse("2026-01-03T23:59:59Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2026-01-04T15:00:17Z");
        influxDbUtils.deleteDataByTimeRange(measurement, sensorId, startTime, endTime);
    }
}
