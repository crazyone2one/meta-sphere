package com.master.meta.service;

import com.master.meta.dto.*;
import com.master.meta.entity.SystemSchedule;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.List;

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

    void removeJob(String key, String job);

    Page<ScheduleDTO> getSchedulePage(SchedulePageRequest request);

    List<SelectOptionDTO> getScheduleNameList(String projectId);

    int updateSchedule(SystemSchedule systemSchedule);

    void enable(String id);

    void updateCron(ScheduleCronRequest request);
}
