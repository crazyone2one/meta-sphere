package com.master.meta;

import com.master.meta.constants.ScheduleType;
import com.master.meta.dto.SelectOptionDTO;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemSchedule;
import com.master.meta.mapper.SystemProjectMapper;
import com.master.meta.service.SystemScheduleService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MetaSphereApplicationTests {
    public static final String DEFAULT_PROJECT_ID = "100001100001";
    @Resource
    private SystemScheduleService scheduleService;
    @Resource
    private SystemProjectMapper projectMapper;

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
        List<SelectOptionDTO> scheduleNameList = scheduleService.getScheduleNameList();
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
}
