package com.master.meta.service;

import com.master.meta.dto.ScheduleConfig;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.SystemSchedule;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * 定时任务 服务层。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
public interface SystemScheduleService extends IService<SystemSchedule> {
    void addSchedule(SystemSchedule schedule);

    void addOrUpdateCronJob(SystemSchedule request, JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> clazz);

    String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> clazz, String operator);

    SystemSchedule getByResourceId(String resourceId);
}
