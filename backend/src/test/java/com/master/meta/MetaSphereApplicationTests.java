package com.master.meta;

import com.master.meta.constants.ScheduleType;
import com.master.meta.constants.SensorMNType;
import com.master.meta.dto.SelectOptionDTO;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemSchedule;
import com.master.meta.mapper.SystemProjectMapper;
import com.master.meta.service.SensorService;
import com.master.meta.service.SystemScheduleService;
import com.master.meta.utils.InfluxDbUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class MetaSphereApplicationTests {
    public static final String DEFAULT_PROJECT_ID = "100001100001";
    @Resource
    private SystemScheduleService scheduleService;
    @Resource
    private SystemProjectMapper projectMapper;
    @Resource
    SensorService sensorService;
    @Resource
    InfluxDbUtils influxDbUtils;

    @Test
    void contextLoads() {
    }

    @Test
    void testAddSchedule() {
        SystemSchedule schedule = new SystemSchedule();
        schedule.setName("test-schedule");
        schedule.setResourceId("test-schedule");
        schedule.setValue("0 0/1 * * * ?");
        schedule.setKey("test-resource-id");
        schedule.setCreateUser("admin");
        schedule.setProjectId(DEFAULT_PROJECT_ID);
        schedule.setJob("com.master.meta.schedule.DemoJob");
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
    }

    @Test
    void testAddOrUpdateCronJob() {
        try {
            SystemSchedule schedule = scheduleService.getById("79905916361000193");
            scheduleService.addOrUpdateCronJob(schedule,
                    new JobKey(schedule.getKey(), schedule.getJob()),
                    new TriggerKey(schedule.getKey(), schedule.getJob()),
                    (Class<? extends Job>) Class.forName(schedule.getJob()));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRemoveJob() {
        SystemSchedule schedule = scheduleService.getById("79905916361000193");
        scheduleService.removeJob("test-resource-id", schedule.getJob());
    }

    @Test
    void testGetClassesInPackage() {
        List<SelectOptionDTO> scheduleNameList = scheduleService.getScheduleNameList("");
        scheduleNameList.forEach(System.out::println);
    }

    @Test
    void testAddProject() {
        SystemProject project = new SystemProject();
        project.setNum("150622004499");
        project.setName("测试项目");
        project.setOrganizationId("100001");
        project.setCreateUser("admin");
        project.setUpdateUser("admin");
        projectMapper.insertSelective(project);
    }

    @Test
    void testInfluxDB() {
        double average = sensorService.averageForTheLastDays("150622004499YSLaJaSWaErG", SensorMNType.SENSOR_SHFZ_YSL, Duration.ofDays(7));
        double sum = sensorService.sumForTheLastDays("150622004499YSLaJaSWaErG", SensorMNType.SENSOR_SHFZ_YSL, Duration.ofHours(24));
        System.out.println("测点【150622004499YSLaJaSWaErG】近24小时总量：" + sum + " ===>近7天平均量：" + average);
        // 计算增长幅度
        if (average != 0) {
            double increaseRate = ((sum - average) / average) * 100;
            System.out.println("增长幅度：" + String.format("%.2f", increaseRate) + "%");

            if (increaseRate > 0) {
                System.out.println("sum比average高" + String.format("%.2f", increaseRate) + "%");
            } else if (increaseRate < 0) {
                System.out.println("sum比average低" + String.format("%.2f", Math.abs(increaseRate)) + "%");
            } else {
                System.out.println("sum与average相等，无增长");
            }
        } else {
            System.out.println("无法计算增长幅度：average为0");
        }
    }

    @Test
    void testCGK() {
        List<String> sensorIdList = Arrays.asList("150622004499MNAEDKamaMj", "150622004499MNArBYqkHRb", "150622004499MNaSPDsQifb", "150622004499MNaxUdmyNOG");
        sensorIdList.forEach(sensorId -> {
            Map<String, Object> stringObjectMap = sensorService.checkWaterLevelVariationCondition(sensorId, SensorMNType.SENSOR_SHFZ_0502);
            log.info("测点【{}】监测结果：{}", sensorId, stringObjectMap);
        });
    }

    @Test
    void deleteDataByTimeRange() {
        String measurement = "sf_shfz_ysl_cdss";
        String sensorId = "150622004499YSLAAEoacbZl";
        OffsetDateTime startTime = OffsetDateTime.parse("2025-11-03T00:00:00Z");
        OffsetDateTime endTime = OffsetDateTime.parse("2025-11-03T15:00:00Z");
        influxDbUtils.deleteDataByTimeRange(measurement, sensorId, startTime, endTime);
    }
}
