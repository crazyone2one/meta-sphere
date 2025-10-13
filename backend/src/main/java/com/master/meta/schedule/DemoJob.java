package com.master.meta.schedule;

import com.master.meta.handle.schedule.BaseScheduleJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Slf4j
public class DemoJob extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String resourceId = jobDataMap.getString("resourceId");
        log.info("DemoJob Running: {}", resourceId);
        log.info("DemoJob Running: {}", super.projectId);
        log.info("DemoJob Running: {}", super.expression);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DemoJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DemoJob.class.getName());
    }
}
