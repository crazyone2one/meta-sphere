package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.FileHelper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Slf4j
public class TestSchedule extends BaseScheduleJob {
    public TestSchedule(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        log.info("{}-{}", super.projectNum, super.projectName);
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, TestSchedule.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, TestSchedule.class.getName());
    }
}
