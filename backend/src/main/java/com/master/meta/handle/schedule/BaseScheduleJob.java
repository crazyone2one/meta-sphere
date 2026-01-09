package com.master.meta.handle.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.dto.ScheduleConfigDTO;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.JSON;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Slf4j
public abstract class BaseScheduleJob implements Job {
    protected String resourceId;
    protected String projectId;
    protected String projectNum;
    protected String projectName;
    protected String userId;
    protected String expression;
    protected String END_FLAG = "||";
    protected ScheduleConfigDTO config;
    protected final SensorService sensorService;
    protected final FileHelper fileHelper;
    protected final FileTransferConfiguration fileTransferConfiguration;

    protected BaseScheduleJob(SensorService sensorService, FileHelper fileHelper, FileTransferConfiguration fileTransferConfiguration) {
        this.sensorService = sensorService;
        this.fileHelper = fileHelper;
        this.fileTransferConfiguration = fileTransferConfiguration;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.resourceId = jobDataMap.getString("resourceId");
        this.userId = jobDataMap.getString("userId");
        this.expression = jobDataMap.getString("expression");
        this.projectId = jobDataMap.getString("projectId");
        this.projectNum = jobDataMap.getString("projectNum");
        this.projectName = jobDataMap.getString("projectName");
        this.config = JSON.objectToType(ScheduleConfigDTO.class).apply(jobDataMap.get("config"));
        businessExecute(context);
    }

    protected abstract void businessExecute(JobExecutionContext context);

    protected List<Row> sourceRows(String key, String tableName) {
        return sensorService.getSensorFromRedis(projectNum, key, tableName);
    }

    protected FileTransferConfiguration.SlaveConfig slaveConfig() {
        return fileTransferConfiguration.getSlaveConfigByResourceId(projectNum);
    }

    ;

    public String fileName(String fileCode, LocalDateTime localDateTime) {
        return projectNum + fileCode + DateFormatUtil.localDateTimeToString(localDateTime) + ".txt";
    }

    public String contentHeader(LocalDateTime localDateTime) {
        return projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(localDateTime) + "~";
    }
}
