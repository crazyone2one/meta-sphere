package com.master.meta.dto;

import com.master.meta.constants.ScheduleType;
import com.master.meta.entity.SystemSchedule;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Data
@Builder
public class ScheduleConfig {
    private String resourceId;

    private String key;

    private String projectId;

    private String name;

    private Boolean enable;

    private String cron;

    private String resourceType;

    private Map<String, String> config;
    public SystemSchedule genCronSchedule(SystemSchedule schedule) {
        if (schedule == null) {
            schedule = new SystemSchedule();
        }
        schedule.setName(this.getName());
        schedule.setResourceId(this.getResourceId());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setKey(this.getKey());
        schedule.setEnable(this.getEnable());
        schedule.setProjectId(this.getProjectId());
        schedule.setValue(this.getCron());
        schedule.setResourceType(this.getResourceType());
        schedule.setConfig(this.getConfig());
        return schedule;
    }
}
