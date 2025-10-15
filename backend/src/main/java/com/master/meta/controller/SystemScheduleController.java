package com.master.meta.controller;

import com.master.meta.dto.*;
import com.master.meta.entity.SystemSchedule;
import com.master.meta.service.SystemScheduleService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 定时任务 控制层。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@RestController
@Tag(name = "定时任务接口")
@RequiredArgsConstructor
@RequestMapping("/system-schedule")
public class SystemScheduleController {

    private final SystemScheduleService systemScheduleService;

    /**
     * 保存定时任务。
     *
     * @param systemSchedule 定时任务
     */
    @PostMapping("save")
    @Operation(description = "保存定时任务")
    public void save(@RequestBody @Parameter(description = "定时任务") SystemSchedule systemSchedule) {
        systemScheduleService.addSchedule(systemSchedule);
    }

    /**
     * 根据主键删除定时任务。
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("remove/{id}")
    @Operation(description = "根据主键删除定时任务")
    public boolean remove(@PathVariable @Parameter(description = "定时任务主键") String id) {
        return systemScheduleService.removeById(id);
    }

    /**
     * 根据主键更新定时任务。
     *
     * @param systemSchedule 定时任务
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("update")
    @Operation(description = "根据主键更新定时任务")
    public boolean update(@RequestBody @Parameter(description = "定时任务主键") SystemSchedule systemSchedule) {
        return systemScheduleService.updateSchedule(systemSchedule);
    }

    /**
     * 查询所有定时任务。
     *
     * @return 所有数据
     */
    @GetMapping("list")
    @Operation(description = "查询所有定时任务")
    public List<SystemSchedule> list() {
        return systemScheduleService.list();
    }

    @GetMapping("/schedule-name-list")
    @Operation(description = "查询所有定时任务")
    public List<SelectOptionDTO> scheduleNameList() {
        return systemScheduleService.getScheduleNameList();
    }

    /**
     * 根据主键获取定时任务。
     *
     * @param id 定时任务主键
     * @return 定时任务详情
     */
    @GetMapping("getInfo/{id}")
    @Operation(description = "根据主键获取定时任务")
    public SystemSchedule getInfo(@PathVariable @Parameter(description = "定时任务主键") String id) {
        return systemScheduleService.getById(id);
    }

    /**
     * 分页查询定时任务。
     *
     * @param request 分页对象
     * @return 分页对象
     */
    @PostMapping("page")
    @Operation(description = "分页查询定时任务")
    public Page<ScheduleDTO> page(@Validated @RequestBody ScheduleRequest request) {
        return systemScheduleService.getSchedulePage(request);
    }

    @PostMapping(value = "/schedule-config")
    @Operation(summary = "定时任务配置")
    public String scheduleConfig(@Validated @RequestBody BaseScheduleConfigRequest request) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SystemSchedule schedule = systemScheduleService.getByResourceId(request.getResourceId());
        ScheduleConfig scheduleConfig = ScheduleConfig.builder()
                .resourceId(schedule.getResourceId())
                .key(schedule.getId())
                .projectId(schedule.getProjectId())
                .name(schedule.getName())
                .enable(request.isEnable())
                .cron(request.getCron())
                .config(request.getRunConfig())
                .build();
        Class<?> targetClass = Class.forName(schedule.getJob());
        Method getJobKey = targetClass.getMethod("getJobKey", String.class);
        Method getTriggerKey = targetClass.getMethod("getTriggerKey", String.class);
        @SuppressWarnings("unchecked")
        Class<? extends Job> jobClass = (Class<? extends Job>) targetClass;
        return systemScheduleService.scheduleConfig(scheduleConfig,
                (JobKey) getJobKey.invoke(null, schedule.getResourceId()),
                (TriggerKey) getTriggerKey.invoke(null, schedule.getResourceId()),
                jobClass,
                "admin");
    }

    @GetMapping("/status/switch/{id}")
    @Operation(summary = "定时任务开启关闭")
    public void enableOrg(@PathVariable String id) {
        systemScheduleService.enable(id);
    }
}
