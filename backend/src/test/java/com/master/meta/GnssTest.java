package com.master.meta;

import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.InfluxDbUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest
public class GnssTest {
    @Resource
    InfluxDbUtils influxDbUtils;

    @Test
    void removeGnssData() {
        String measurement = "aqjk_gnss_monitor";
        String sensorId = "15062202000102I8hjwK";
        OffsetDateTime startTime = OffsetDateTime.parse("2025-11-30T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2025-12-27T00:00:00Z");
        influxDbUtils.deleteGnssDataByTimeRange(measurement, null, startTime, endTime);
    }

    @Test
    void getGnssData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.of(now.getYear(), now.getMonth().minus(1), 1, 0, 0, 0);
        // 月份结束时间：年-月-最后一天 23:59:59
        LocalDateTime endTime = LocalDate.of(now.getYear(), now.getMonth().minus(1), 1)
                .plusMonths(1)
                .minusDays(1)
                .atTime(LocalTime.MAX);
        String startTimeStr = DateFormatUtil.getUTCByLocal(startTime);
        String endTimeStr = DateFormatUtil.getUTCByLocal(endTime);
        String query = "|> range(start: " + startTimeStr + ",stop:" + endTimeStr + ")" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"aqjk_gnss_monitor\")" +
                " |> filter(fn: (r) => r[\"equip_no\"] == \"15062202000102MaX5IZ\")" +
                " |> filter(fn: (r) => r[\"_field\"] == \"x_disp\" or r[\"_field\"] == \"y_disp\" or r[\"_field\"] == \"z_disp\")" +
                " |> yield(name: \"mean\")";
        List<FluxTable> data = influxDbUtils.getData(query);
        AtomicReference<Double> totalX = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalY = new AtomicReference<>((double) 0);
        AtomicReference<Double> totalZ = new AtomicReference<>((double) 0);
        AtomicReference<Integer> sensorCount = new AtomicReference<>(0);
        for (FluxTable table : data) {
            List<FluxRecord> records = table.getRecords();
            records.forEach(record -> {
                Optional.ofNullable(record.getValue()).ifPresent(s -> {
                    totalX.updateAndGet(v -> v + Double.parseDouble((String) s));
                    sensorCount.updateAndGet(v -> v + 1);
                });
                Optional.ofNullable(record.getValue()).ifPresent(s -> totalY.updateAndGet(v -> v + Double.parseDouble((String) s)));
                Optional.ofNullable(record.getValue()).ifPresent(s -> totalZ.updateAndGet(v -> v + Double.parseDouble((String) s)));
            });
        }
        // System.out.println("X: " + totalX.get() + " Y: " + totalY.get() + " Z: " + totalZ.get());
        double result = Math.sqrt(Math.pow(totalX.get(), 2) + Math.pow(totalY.get(), 2) + Math.pow(totalZ.get(), 2));
        // System.out.println("平方和开根号结果: " + result);
        // System.out.println("传感器数量: " + sensorCount.get());
        // System.out.println("平均值: " + result / sensorCount.get());
        // 计算阈值
        double threshold = (result / sensorCount.get()) * 0.25;
        // System.out.println("原始阈值: " + threshold);
        // 生成满足条件的三个数
        double factor = 1.2; // 使用1.2倍确保大于阈值
        double generatedX = threshold * factor / Math.sqrt(3); // 除以√3确保x²+y²+z²的开根号大于阈值
        double generatedY = threshold * factor / Math.sqrt(3);
        double generatedZ = threshold * factor / Math.sqrt(3);

        double generatedMagnitude = Math.sqrt(Math.pow(generatedX, 2) + Math.pow(generatedY, 2) + Math.pow(generatedZ, 2));
        System.out.println("生成的三个数: X=" + generatedX + ", Y=" + generatedY + ", Z=" + generatedZ);
        System.out.println("生成数的向量模长: " + generatedMagnitude);
        System.out.println("是否满足条件: " + (generatedMagnitude > threshold));
    }
}
