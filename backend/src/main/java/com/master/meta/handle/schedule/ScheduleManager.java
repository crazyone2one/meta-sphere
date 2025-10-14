package com.master.meta.handle.schedule;

import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemSchedule;
import com.master.meta.handle.exception.CustomException;
import com.mybatisflex.core.query.QueryChain;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Slf4j
@Component
public class ScheduleManager {
    private final Scheduler scheduler;

    public ScheduleManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * 添加基于Cron表达式的定时任务
     *
     * @param jobKey     任务的唯一标识，包含任务名称和组名
     * @param triggerKey 触发器的唯一标识，包含触发器名称和组名
     * @param jobClass   需要执行的任务类，必须实现Job接口
     * @param cron       Cron表达式，定义任务执行的时间规则
     * @param jobDataMap 任务执行时需要传递的数据映射，可为null
     */
    public void addCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap) {
        try {
            log.info("addCronJob: {},{}", triggerKey.getName(), triggerKey.getGroup());
            JobBuilder jobBuilder = JobBuilder.newJob(jobClass).withIdentity(jobKey);
            if (jobDataMap != null) {
                jobBuilder.usingJobData(jobDataMap);
            }
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            triggerBuilder.withIdentity(triggerKey);
            triggerBuilder.startNow();
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            scheduler.scheduleJob(jobBuilder.build(), trigger);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new CustomException("定时任务配置异常: " + e.getMessage());
        }
    }

    /**
     * 修改定时任务的Cron表达式时间
     *
     * @param triggerKey 触发器的唯一标识，包含触发器名称和组名
     * @param cron       新的Cron表达式，定义任务执行的时间规则
     */
    public void modifyCronJobTime(TriggerKey triggerKey, String cron) {
        log.info("modifyCronJobTime: {},{}", triggerKey.getName(), triggerKey.getGroup());
        try {
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                /* 方式一 ：调用 rescheduleJob 开始 */
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();// 触发器
                triggerBuilder.withIdentity(triggerKey);// 触发器名,触发器组
                triggerBuilder.startNow(); // 立即执行
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron)); // 触发器时间设定
                trigger = (CronTrigger) triggerBuilder.build(); // 创建Trigger对象
                scheduler.rescheduleJob(triggerKey, trigger); // 修改一个任务的触发时间
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void removeJob(JobKey jobKey, TriggerKey triggerKey) {
        try {
            log.info("RemoveJob: {},{}", jobKey.getName(), jobKey.getGroup());
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);
            scheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void startJobs(Scheduler schedule) {
        try {
            schedule.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void shutdownJobs(Scheduler schedule) {
        try {
            if (!schedule.isShutdown()) {
                schedule.shutdown();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void addOrUpdateCronJob(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap)
            throws SchedulerException {
        log.info("AddOrUpdateCronJob: {},{}", jobKey.getName(), triggerKey.getGroup());
        if (scheduler.checkExists(triggerKey)) {
            modifyCronJobTime(triggerKey, cron);
        } else {
            addCronJob(jobKey, triggerKey, jobClass, cron, jobDataMap);
        }
    }

    public JobDataMap getDefaultJobDataMap(SystemSchedule schedule, String expression, String userId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("resourceId", schedule.getResourceId());
        jobDataMap.put("expression", expression);
        jobDataMap.put("userId", userId);
        jobDataMap.put("config", schedule.getConfig());
        jobDataMap.put("projectId", schedule.getProjectId());
        SystemProject systemProject = QueryChain.of(SystemProject.class).where(SystemProject::getId).eq(schedule.getProjectId()).one();
        jobDataMap.put("projectNum", systemProject.getNum());
        jobDataMap.put("projectName", systemProject.getName());
        return jobDataMap;
    }
}
