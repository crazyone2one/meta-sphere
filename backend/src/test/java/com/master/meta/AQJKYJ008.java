package com.master.meta;

import com.master.meta.constants.SensorMNType;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.InfluxDbUtils;
import com.mybatisflex.core.row.Row;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/3
 */
@Slf4j
@SpringBootTest
public class AQJKYJ008 {
    @Resource
    SensorService sensorService;
    @Resource
    InfluxDbUtils influxDbUtils;

    @Test
    void testBlue() {
        String sensorId = "150622004499YSLAAEoacbZl";
        test(sensorId, 30);
        test(sensorId, 50);
        test(sensorId, 80);
        test(sensorId, 100);
    }

    private void test(String sensorId, double flag) {
        // 近1小时总量
        double sum = sensorService.sumForTheLastDays(sensorId, SensorMNType.SENSOR_SHFZ_YSL, Duration.ofHours(1));
        // 近7天总量
        double sum7days = sensorService.sumForTheLastDays(sensorId, SensorMNType.SENSOR_SHFZ_YSL, Duration.ofDays(7));
        // 近7天平均值
        double average7days = sum7days / (24 * 7);
        // 近7天涌水量的平均值增加幅度 30%
        double res = ((flag + 100) / 100) * average7days;
        // 近1小时总量与增加幅度差值
        double value = sum - res;
        log.info("每小时涌水量{}与增幅[{}]相差{}", sum, flag, value);
    }

    @Test
    void testRed() {
        List<Row> parsedObject = sensorService.getShfzSensorList(SensorMNType.SENSOR_SHFZ_YSL.getTableName(), false);
        List<Row> sensorList = parsedObject.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("deleted"))).toList();
        List<Row> deleted = parsedObject.stream().filter(row -> BooleanUtils.isTrue(row.getBoolean("deleted"))).toList();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String content = "150622004499;xxxx;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(sensorList, now)
                + newContent(deleted, now)
                + "||";
        log.info("文件内容：{}", content);
    }

    @Test
    void removeInfluxData() {
        String measurement = "sf_shfz_ysl_cdss";
        String sensorId = "150622004499YSLAAEoacbZl";
        OffsetDateTime startTime = OffsetDateTime.parse("2025-11-04T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2025-11-04T15:30:00Z");
        influxDbUtils.deleteDataByTimeRange(measurement, sensorId, startTime, endTime);
    }

    private String newContent(List<Row> deleted, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        Row row = deleted.getFirst();
        String sensor = row.getString("id") + ";"
                + row.getString("install_location") + ";"
                + row.getString("coverage") + ";"
                + row.getString("sensor_type") + ";"
                + row.getString("measurement_point_unit") + ";"
                + "4245615.60;36372560.60;1229.00;" + DateFormatUtil.localDateTime2StringStyle3(localDateTime) + "~";
        content.append(sensor);
        return content.toString();
    }

    private String bodyContent(List<Row> sensorList, LocalDateTime localDateTime) {
        StringBuilder content = new StringBuilder();
        sensorList.forEach(row -> {
            String sensor = row.getString("id") + ";"
                    + row.getString("install_location") + ";"
                    + row.getString("coverage") + ";"
                    + row.getString("sensor_type") + ";"
                    + row.getString("measurement_point_unit") + ";"
                    + "4245615.60;36372560.60;1229.00;" + DateFormatUtil.localDateTime2StringStyle3(localDateTime) + "~";
            content.append(sensor);
        });
        return content.toString();
    }
}
