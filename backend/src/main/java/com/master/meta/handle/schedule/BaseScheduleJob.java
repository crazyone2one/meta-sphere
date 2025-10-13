package com.master.meta.handle.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Slf4j
public abstract class BaseScheduleJob implements Job {
    protected String resourceId;
    protected String projectId;
    protected String userId;
    protected String expression;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        this.projectId = jobDataMap.getString("projectId");
        log.info("{} Running: {}", jobKey.getGroup(), resourceId);
        businessExecute(context);
    }

    protected abstract void businessExecute(JobExecutionContext context);
}
