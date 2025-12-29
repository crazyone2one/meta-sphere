package com.master.meta.schedule.lt;

import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.WkkSensorEnum;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.*;
import com.mybatisflex.core.row.Row;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class GNSSRealTime extends BaseScheduleJob {
    private final SensorUtil sensorUtil;
    private final FileTransferConfiguration fileTransferConfiguration;
    private final InfluxDbUtils influxDbUtils;

    private GNSSRealTime(SensorUtil sensorUtil, FileTransferConfiguration fileTransferConfiguration, InfluxDbUtils influxDbUtils) {
        this.sensorUtil = sensorUtil;
        this.fileTransferConfiguration = fileTransferConfiguration;
        this.influxDbUtils = influxDbUtils;
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        FileTransferConfiguration.SlaveConfig slaveConfig = fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
        List<Row> sourceRows = sensorUtil.getWkkFromRedis(projectNum, WkkSensorEnum.GNSSREALRIME.getKey(), WkkSensorEnum.GNSSREALRIME.getTableName(), false);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        List<Row> effectiveSensor = sourceRows.stream()
                .filter(s -> BooleanUtils.isFalse(s.getBoolean("deleted")))
                .filter(s -> s.getInt("in_use") == 1)
                .filter(s -> s.getInt("breakdown") == 0)
                .toList();
        if (CollectionUtils.isEmpty(effectiveSensor)) {
            return;
        }

        String fileName = projectNum + "_" + WkkSensorEnum.GNSSREALRIME.getCdssKey() + "_"
                + DateFormatUtil.localDateTimeToString(now) + "_"
                + RandomUtil.generateRandomIntegerByLength(4) + ".json";
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("send_time", DateFormatUtil.localDateTime2StringStyle2(now));
        content.put("open_pit_no", projectNum);
        content.put("data", contentData(effectiveSensor, now));
        // String filePath = "/app/files/gnss/" + fileName;
        String filePath = sensorUtil.filePath(slaveConfig.getLocalPath(), projectNum, "gnss", fileName);
        sensorUtil.generateFile(filePath, JSON.toJSONString(content), "gnss实时信息[" + fileName + "]");
        // sensorUtil.uploadFile(filePath, "/home/app/ftp/GNSS");
        sensorUtil.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + File.separator + "GNSS");
    }

    private List<Map<String, Object>> contentData(List<Row> sensorInRedis, LocalDateTime now) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sensorCode = config.getField("sensorCode", String.class);
        AtomicReference<String> disp = new AtomicReference<>("1.0");
        for (Row s : sensorInRedis) {
            String equipNo = s.getString("equip_no");
            if (!equipNo.equals(sensorCode)) {
                continue;
            }
            disp.set(config.getField("disp", String.class));
            Map<String, Object> content = new LinkedHashMap<>();
            content.put("equip_no", equipNo);
            content.put("monitor_time", DateFormatUtil.localDateTime2StringStyle2(now));
            content.put("longitude", s.getString("longitude"));
            content.put("latitude", s.getString("latitude"));
            content.put("altitude", s.getString("altitude"));
            content.put("x_disp", disp.get());
            content.put("y_disp", disp.get());
            content.put("z_disp", disp.get());
            content.put("x_speed", "1.4");
            content.put("y_speed", "1.5");
            content.put("z_speed", "1.6");
            content.put("x_acc_speed", "1.7");
            content.put("y_acc_speed", "1.8");
            content.put("z_acc_speed", "1.9");
            result.add(content);
        }

        return result;
    }

    /**
     * 根据上个月测点数据的平均值生成测点数据
     *
     */
    private Double generateDispValue(LocalDateTime now) {
        LocalDateTime startTime = LocalDateTime.of(now.getYear(), now.getMonth().minus(1), 1, 0, 0, 0);
        LocalDateTime endTime = LocalDate.of(now.getYear(), now.getMonth().minus(1), 1)
                .plusMonths(1)
                .minusDays(1)
                .atTime(LocalTime.MAX);
        String startTimeStr = DateFormatUtil.getUTCByLocal(startTime);
        String endTimeStr = DateFormatUtil.getUTCByLocal(endTime);
        String query = "|> range(start: " + startTimeStr + ",stop:" + endTimeStr + ")" +
                " |> filter(fn: (r) => r[\"_measurement\"] == \"aqjk_gnss_monitor\")" +
                " |> filter(fn: (r) => r[\"equip_no\"] == \"15062202000102I8hjwK\")" +
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
                Optional.ofNullable(record.getValue()).ifPresent(s -> totalX.updateAndGet(v -> v + Double.parseDouble((String) s)));
                Optional.ofNullable(record.getValue()).ifPresent(s -> totalY.updateAndGet(v -> v + Double.parseDouble((String) s)));
                Optional.ofNullable(record.getValue()).ifPresent(s -> totalZ.updateAndGet(v -> v + Double.parseDouble((String) s)));
                sensorCount.updateAndGet(v -> v + 1);
            });
        }
        System.out.println("last month record count:" + sensorCount.get());
        double result = Math.sqrt(Math.pow(totalX.get(), 2) + Math.pow(totalY.get(), 2) + Math.pow(totalZ.get(), 2));
        // BigDecimal threshold = BigDecimal.valueOf((result / sensorCount.get())).multiply(config.getField("threshold", BigDecimal.class));
        // 安全的类型转换方式
        String thresholdObj = config.getField("threshold", String.class);
        // 计算阈值
        double threshold = (result / sensorCount.get()) * Double.parseDouble(thresholdObj);
        System.out.println("原始阈值: " + threshold);
        double factor = 1.2; // 使用1.2倍确保大于阈值
        double v = threshold * factor / Math.sqrt(3);
        System.out.println("阈值为：" + v);
        return v; // 除以√3确保x²+y²+z²的开根号大于阈值
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, GNSSRealTime.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, GNSSRealTime.class.getName());
    }
}
