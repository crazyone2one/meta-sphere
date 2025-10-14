package com.master.meta.schedule;

import com.master.meta.handle.schedule.BaseScheduleJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * @author Created by 11's papa on 2025/10/13
 */
public class TestSchedule extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        Object config = jobDataMap.get("config");
        System.out.println(config);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, TestSchedule.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, TestSchedule.class.getName());
    }
}
