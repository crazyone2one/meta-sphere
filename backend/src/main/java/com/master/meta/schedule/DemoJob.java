package com.master.meta.schedule;


import com.fasterxml.jackson.core.type.TypeReference;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.utils.JSON;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Slf4j
public class DemoJob extends BaseScheduleJob {
    @Override
    protected void businessExecute(JobExecutionContext context) {
        List<LocalDateTime> dateTimeList = JSON.objectToType(new TypeReference<List<Long>>() {
                })
                .apply(super.config.getAdditionalFields().get("range"))
                .stream()
                .map(timestamp -> LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()))
                .toList();

        if (super.config.isYcFlag()) {
            LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8")).withSecond(0).withNano(0);
            LocalDateTime start = dateTimeList.get(0);
            LocalDateTime end = dateTimeList.get(1);
            if (now.equals(start)) {
                log.info("DemoJob YC: {}", "YC begin");
            } else if (now.isAfter(start) && now.isBefore(end)) {
                log.info("DemoJob ing: {}", "YC");
            } else if (now.equals(end)) {
                log.info("DemoJob end: {}", "alarm over");
            }
        }
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, DemoJob.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, DemoJob.class.getName());
    }
}
