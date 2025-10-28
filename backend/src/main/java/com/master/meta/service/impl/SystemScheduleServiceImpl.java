package com.master.meta.service.impl;

import com.master.meta.constants.ApplicationNumScope;
import com.master.meta.constants.SensorMNType;
import com.master.meta.dto.*;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemSchedule;
import com.master.meta.handle.ClassScanner;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.schedule.ScheduleManager;
import com.master.meta.mapper.SystemScheduleMapper;
import com.master.meta.service.SystemScheduleService;
import com.master.meta.uid.NumGenerator;
import com.master.meta.utils.SensorUtil;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.row.Row;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemScheduleTableDef.SYSTEM_SCHEDULE;

/**
 * 定时任务 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@Slf4j
@Service
public class SystemScheduleServiceImpl extends ServiceImpl<SystemScheduleMapper, SystemSchedule> implements SystemScheduleService {
    private final ScheduleManager scheduleManager;
    private final SensorUtil sensorUtil;
    private final ClassScanner classScanner;

    public SystemScheduleServiceImpl(ScheduleManager scheduleManager,
                                     SensorUtil sensorUtil,
                                     ClassScanner classScanner) {
        this.scheduleManager = scheduleManager;
        this.sensorUtil = sensorUtil;
        this.classScanner = classScanner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSchedule(SystemSchedule schedule) {
        schedule.setNum(getNextNum(schedule.getProjectId()));
        schedule.setCreateUser(SessionUtils.getUserName());
        mapper.insertSelective(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrUpdateCronJob(SystemSchedule request, JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> clazz) {
        Boolean enable = request.getEnable();
        String cronExpression = request.getValue();
        if (BooleanUtils.isTrue(enable) && StringUtils.isNotBlank(cronExpression)) {
            try {
                scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, clazz, cronExpression,
                        scheduleManager.getDefaultJobDataMap(request, cronExpression, request.getCreateUser()));
            } catch (SchedulerException e) {
                throw new CustomException("定时任务开启异常: " + e.getMessage());
            }
        } else {
            try {
                scheduleManager.removeJob(jobKey, triggerKey);
            } catch (Exception e) {
                throw new CustomException("定时任务关闭异常: " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String scheduleConfig(ScheduleConfig scheduleConfig, JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> clazz, String operator) {
        SystemSchedule schedule;
        QueryChain<SystemSchedule> scheduleQueryChain = queryChain()
                .where(SYSTEM_SCHEDULE.RESOURCE_ID.eq(scheduleConfig.getResourceId())
                        .and(SYSTEM_SCHEDULE.JOB.eq(clazz.getName())));
        List<SystemSchedule> scheduleList = scheduleQueryChain.list();
        if (CollectionUtils.isNotEmpty(scheduleList)) {
            schedule = scheduleConfig.genCronSchedule(scheduleList.getFirst());
            schedule.setJob(clazz.getName());
            mapper.updateByQuery(schedule, scheduleQueryChain);
        } else {
            schedule = scheduleConfig.genCronSchedule(null);
            schedule.setJob(clazz.getName());
            schedule.setCreateUser(operator);
            schedule.setNum(getNextNum(scheduleConfig.getProjectId()));
            mapper.insertSelective(schedule);
        }
        JobDataMap jobDataMap = scheduleManager.getDefaultJobDataMap(schedule, scheduleConfig.getCron(), schedule.getCreateUser());
        scheduleManager.removeJob(jobKey, triggerKey);
        if (BooleanUtils.isTrue(schedule.getEnable())) {
            scheduleManager.addCronJob(jobKey, triggerKey, clazz, scheduleConfig.getCron(), jobDataMap);
        }
        return schedule.getId();
    }

    @Override
    public SystemSchedule getByResourceId(String resourceId) {
        return queryChain().where(SYSTEM_SCHEDULE.RESOURCE_ID.eq(resourceId)).one();
    }

    @Override
    public void removeJob(String key, String job) {
        scheduleManager.removeJob(new JobKey(key, job), new TriggerKey(key, job));
    }

    @Override
    public Page<ScheduleDTO> getSchedulePage(SchedulePageRequest request) {
        QueryChain<SystemSchedule> systemScheduleQueryChain = queryChain()
                .select(SYSTEM_SCHEDULE.ID, SYSTEM_SCHEDULE.NAME, SYSTEM_SCHEDULE.ENABLE, SYSTEM_SCHEDULE.VALUE)
                .select(SYSTEM_SCHEDULE.CREATE_USER, SYSTEM_SCHEDULE.CREATE_TIME, SYSTEM_SCHEDULE.NUM, SYSTEM_SCHEDULE.PROJECT_ID)
                .select(SYSTEM_SCHEDULE.RESOURCE_ID, SYSTEM_SCHEDULE.CONFIG.as("runConfig"))
                .select(SYSTEM_SCHEDULE.RESOURCE_TYPE, SYSTEM_SCHEDULE.SENSOR_GROUP)
                .select(SYSTEM_PROJECT.NAME.as("projectName"))
                .select("QRTZ_TRIGGERS.PREV_FIRE_TIME AS last_time")
                .select("QRTZ_TRIGGERS.NEXT_FIRE_TIME AS nextTime")
                .from(SYSTEM_SCHEDULE)
                .leftJoin(SYSTEM_PROJECT).on(SYSTEM_PROJECT.ID.eq(SYSTEM_SCHEDULE.PROJECT_ID))
                .leftJoin("QRTZ_TRIGGERS").on("QRTZ_TRIGGERS.TRIGGER_NAME = system_schedule.resource_id");
        return systemScheduleQueryChain
                .where(SYSTEM_SCHEDULE.NAME.like(request.getKeyword()).or(SYSTEM_SCHEDULE.NUM.like(request.getKeyword())))
                .and(SYSTEM_SCHEDULE.PROJECT_ID.eq(request.getProjectId()))
                .and(SYSTEM_SCHEDULE.RESOURCE_TYPE.eq(request.getResourceType()))
                .pageAs(new Page<>(request.getPage(), request.getPageSize()), ScheduleDTO.class);
    }

    @Override
    public List<SelectOptionDTO> getScheduleNameList(String projectId) {
        List<String> allJobNames = classScanner.scanPackage("com.master.meta.schedule");
        List<String> existJobNames = queryChain().select(SYSTEM_SCHEDULE.JOB)
                .where(SYSTEM_SCHEDULE.PROJECT_ID.eq(projectId)).listAs(String.class);
        return allJobNames.stream()
                .filter(jobName -> !existJobNames.contains(jobName)) // 包含时跳过
                .map(jobName -> {
                    SelectOptionDTO option = new SelectOptionDTO();
                    option.setLabel(jobName);
                    option.setValue(jobName);
                    return option;
                }).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSchedule(SystemSchedule systemSchedule) {
        return mapper.insertOrUpdateSelective(systemSchedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(String id) {
        SystemSchedule schedule = checkScheduleExit(id);
        schedule.setEnable(!schedule.getEnable());
        mapper.update(schedule);
        try {
            extracted4UpdateJob(schedule);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCron(ScheduleCronRequest request) {
        SystemSchedule schedule = checkScheduleExit(request.getId());
        schedule.setValue(request.getCron());
        mapper.update(schedule);
        try {
            extracted4UpdateJob(schedule);
        } catch (ClassNotFoundException e) {
            throw new CustomException("找不到定时任务类: " + schedule.getJob());
        }
    }

    @Override
    public List<SensorSelectOptionDTO> getSensorOptions(BaseCondition request) {
        SystemProject systemProject = QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.ID.eq(request.getProjectId())).oneOpt()
                .orElseThrow(() -> new CustomException("<项目不存在>"));
        List<Row> sensorFromRedis = sensorUtil.getCDSSSensorFromRedis(systemProject.getNum(), SensorMNType.SENSOR_AQJK_CO, false);
        if (CollectionUtils.isEmpty(sensorFromRedis)) {
            return new ArrayList<>();
        }

        List<Row> sensorList = sensorFromRedis.stream()
                .filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete")))
                .filter(row -> {
                    // 排除传感器类型为1003、1008和1010的数据
                    String sensorType = row.getString("sensor_type");
                    return !("1003".equals(sensorType) || "1008".equals(sensorType) || "1010".equals(sensorType));
                })
                .toList();
        return sensorList.stream()
                .map(row -> new SensorSelectOptionDTO(row.getString("sensor_location"),
                        row.getString("sensor_code"),
                        row.getString("sensor_code"),
                        row.getString("sensor_location"),
                        row.getString("sensor_type"),
                        row.getString("sensor_value_type"),
                        row.getString("sensor_value_unit"),
                        row.getBoolean("is_delete"))
                ).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScheduleTask(String id) {
        SystemSchedule schedule = checkScheduleExit(id);
        mapper.delete(schedule);
        removeJob(schedule.getResourceId(), schedule.getJob());
    }

    @Transactional(rollbackFor = Exception.class)
    protected void extracted4UpdateJob(SystemSchedule schedule) throws ClassNotFoundException {
        Class<?> jobClass = Class.forName(schedule.getJob());
        if (Job.class.isAssignableFrom(jobClass)) {
            @SuppressWarnings("unchecked")
            Class<? extends Job> jobClassCast = (Class<? extends Job>) jobClass;
            addOrUpdateCronJob(schedule, new JobKey(schedule.getResourceId(), schedule.getJob()),
                    new TriggerKey(schedule.getResourceId(), schedule.getJob()), jobClassCast);
        } else {
            throw new CustomException("指定的类不是有效的Job类: " + schedule.getJob());
        }
    }

    private SystemSchedule checkScheduleExit(String id) {
        return queryChain().where(SYSTEM_SCHEDULE.ID.eq(id)).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("schedule_not_exist")));
    }

    private Long getNextNum(String projectId) {
        return NumGenerator.nextNum(projectId, ApplicationNumScope.TASK);
    }
}
