package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.*;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
public class RYSSInfo extends BaseScheduleJob {
    private final RedisService redisService;

    private RYSSInfo(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileManager fileManager, RedisService redisService) {
        super(sensorService, fileManager, fileTransferConfiguration);
        this.redisService = redisService;
    }

    private static final String SUBSTATION_CACHE_KEY = "substation_list";
    private static final String SUBSTATION_IN_USE_CACHE_KEY = "substation_in_use_list";
    private static final long CACHE_TIMEOUT = 60 * 60 * 24L;
    private static final List<String> ignorePersonPost = Arrays.asList("石家庄墨隆", "鲁新煤矿", "奥瑞森特");

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        String fileName = "150622B0012000200092_RYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        if (config.isFmFlag()) {
            fileName = projectNum + "_NRYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        }
        String headerContent = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "^";
        if (!config.isFmFlag()) {
            headerContent = DateFormatUtil.localDateTime2StringStyle2(now) + ";309;150622B001200020009201307/zkah;~";
        }
        // 0表示空内容-没有人员信息
        boolean emptyContentFlag = "0".equals(Optional.ofNullable(config.getField("emptyContent", String.class)).orElse("1"));
        String bodyContent = "";
        List<ShiftUtils.ShiftPeriod> shifts = ShiftUtils.createStandardThreeShifts();
        // 当前所在班次
        ShiftUtils.ShiftPeriod currentShift = ShiftUtils.getCurrentShift(now, shifts);
        List<Row> personList = getPersonList(currentShift);
        if (!emptyContentFlag) {
            val substationList = getSubstationList();
            val areaList = getAreaList();
            bodyContent = bodyContent(substationList, areaList, now, personList, currentShift);
        }

        StringBuilder content = new StringBuilder();
        content.append(headerContent);
        content.append(emptyContentFlag ? "" : bodyContent);
        content.append(config.isFmFlag() ? "]]]" : END_FLAG);
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String filePath = fileManager.buildFilePath(slaveConfig.getLocalPath(), projectNum, "rydw", fileName);
        fileManager.writeToFile(filePath, content.toString(), "实时数据[" + fileName + "]");
        fileManager.uploadAndCleanup(slaveConfig, filePath, slaveConfig.getRemotePath() + "rydw");
        // 在文件处理完成后执行删除操作
        deletePersonBehaviorAtShiftEnd(now, personList);
    }

    private void deletePersonBehaviorAtShiftEnd(LocalDateTime now, List<Row> personList) {
        List<ShiftUtils.ShiftPeriod> shifts = ShiftUtils.createStandardThreeShifts();

        // 首先尝试获取即将结束的班次（处理班次交接点的情况）
        ShiftUtils.ShiftPeriod endingShift = ShiftUtils.getEndingShiftAtTime(now, shifts);

        if (endingShift != null) {
            // 在班次交接点，删除即将结束班次的人员数据
            for (Row person : personList) {
                String personCode = person.getString("person_code");
                sensorService.deletePersonBehavior(personCode);
                redisService.delete(projectNum + ":" + SUBSTATION_IN_USE_CACHE_KEY + ":" + personCode);
            }
        } else {
            // 非交接点，使用原有逻辑
            ShiftUtils.ShiftPeriod currentShift = ShiftUtils.getCurrentShift(now, shifts);
            if (currentShift != null && ShiftUtils.isCurrentTimeEqualShiftEndTime(now, currentShift)) {
                for (Row person : personList) {
                    String personCode = person.getString("person_code");
                    sensorService.deletePersonBehavior(personCode);
                    redisService.delete(projectNum + ":" + SUBSTATION_IN_USE_CACHE_KEY + ":" + personCode);
                }
            }
        }
    }

    private String bodyContent(List<Row> substationList, List<Row> areaList,
                               LocalDateTime now,
                               List<Row> personList,
                               ShiftUtils.ShiftPeriod currentShift) {
        StringBuilder content = new StringBuilder();

        LocalDateTime inDate = ShiftUtils.getShiftStartDateTime(currentShift, now);
        LocalDateTime shiftEndTime = ShiftUtils.getShiftEndDateTime(currentShift, now);
        String excludePersonCode = config.getField("excludePersonCode", String.class);
        List<String> excludePersonCodeList = new ArrayList<>();
        if (excludePersonCode.isBlank()) {
            excludePersonCodeList = Arrays.stream(excludePersonCode.split(",")).toList();
        }

        for (Row person : personList) {
            String personCode = person.getString("person_code");
            if (excludePersonCodeList.contains(personCode)) {
                continue;
            }
            // 从缓存获取该人员的可用基站列表
            List<Row> availableStations = getAvailableStationsForPerson(personCode, substationList);
            // List<String> substationCode = availableStations.stream().map(station -> station.getString("station_num")).toList();
            Row substation = availableStations.getFirst();
            Row area = RandomUtil.getRandomSubList(areaList, 1).getFirst();
            // 班次结束标记
            boolean shiftEndFlag = ShiftUtils.isCurrentTimeEqualShiftEndTime(now, currentShift);
            content.append(personCode).append(";");
            content.append(person.getString("person_name")).append(";");
            content.append(shiftEndFlag ? "2" : "1").append(";").append(DateFormatUtil.localDateTime2StringStyle2(inDate)).append(";");
            String outTime = config.isFmFlag() ? "" : "xxxx-xx-xx xx:xx:xx";
            if (shiftEndFlag) {
                outTime = DateFormatUtil.localDateTime2StringStyle2(shiftEndTime);
            }
            content.append(outTime).append(";");
            content.append(area.getString(config.isFmFlag() ? "area_type" : "area_code")).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
            content.append(substation.getString("station_code")).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
            if (!config.isFmFlag()) {
                // content.append("默认班制;16.21;正常;0;0;");
                content.append(behavior(substation, now, personCode));
            } else {
                content.append("1;1;10;1;");
                content.append(";;;");
                content.append("0;0;1;");
                content.append(";;");
            }
            content.append(config.isFmFlag() ? "^" : "~");
            // if (shiftEndFlag) {
            //     sensorService.deletePersonBehavior(personCode);
            // }
            availableStations.remove(substation);
            updatePersonStationCache(personCode, availableStations);
        }
        return content.toString();
    }

    private void updatePersonStationCache(String personCode, List<Row> remainingStations) {
        String cacheKey = projectNum + ":" + SUBSTATION_IN_USE_CACHE_KEY + ":" + personCode;
        redisService.setObj(cacheKey, JSON.toJSONString(remainingStations), CACHE_TIMEOUT);
    }

    private List<Row> getAvailableStationsForPerson(String personCode, List<Row> fullStationList) {
        String cacheKey = projectNum + ":" + SUBSTATION_IN_USE_CACHE_KEY + ":" + personCode;
        String stationInUse = redisService.getObj(cacheKey);
        if (StringUtils.isNotEmpty(stationInUse)) {
            try {
                List<Row> cachedStations = JSON.parseArray(stationInUse, Row.class);
                if (CollectionUtils.isNotEmpty(cachedStations)) {
                    return cachedStations;
                }
            } catch (Exception e) {
                log.warn("解析Redis缓存的分站列表失败,使用完整列表: {}", e.getMessage());
            }
        }
        // 缓存不存在、为空或解析失败时,返回完整基站列表的副本
        return new ArrayList<>(fullStationList);
    }

    private String behavior(Row substation, LocalDateTime now, String personCode) {
        StringBuilder content = new StringBuilder();
        String personBehavior = sensorService.getPersonBehavior(personCode);
        if (StringUtils.isNotEmpty(personBehavior)) {
            content.append(personBehavior).append(",");
        }
        content.append(substation.getString("station_code")).append("&")
                .append(DateFormatUtil.localDateTime2StringStyle2(now))
                .append(",");
        if (!content.isEmpty()) {
            content.deleteCharAt(content.length() - 1);
        }
        sensorService.setPersonBehavior(personCode, content.toString());
        return content.toString();
    }

    public List<Row> getPersonList(ShiftUtils.ShiftPeriod currentShift) {
        List<Row> rows;
        Random random = new Random();
        val nextInt = random.nextInt(1, 5);
        Integer personCount = Optional.ofNullable(config.getField("personCount", Integer.class)).orElse(5);
        String personCode = Optional.ofNullable(config.getField("personCode", String.class)).orElse("150623B00120001002620");
        // List<String> excludePersonName = new ArrayList<>();
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            QueryWrapper condition = new QueryWrapper()
                    .select("id", "person_code", "person_name")
                    .ne("id_number", "")
                    .ne("is_delete", "1")
                    .likeLeft("person_code", personCode + currentShift.shiftType())
                    .notLike("person_name", "厂家")
                    .notLike("person_name", "贵宾")
                    .notLike("person_name", "华电")
                    .notLike("person_name", "测试")
                    .notLike("person_name", "调度室")
                    .notLike("person_name", "检查组")
                    .notLike("person_post", "山东")
                    .notLike("person_post", "内蒙能源")
                    .notLike("person_post", "前旗")
                    .notLike("person_post", "长城")
                    .notLike("person_post", "综合办公室")
                    .notLike("person_post", "新矿集团")
                    .notLike("person_post", "榆树井")
                    .notLike("person_post", "翟镇")
                    .notLike("person_post", "郑煤")
                    .notLike("person_post", "综合服务")
                    .notLike("person_post", "一级标准化")
                    .notLike("person_post", "四长")
                    .notLike("person_post", "枣矿")
                    .notLike("person_post", "厂家")
                    .notLike("person_post", "兖矿");
            ignorePersonPost.forEach(p -> condition.notLike("person_post", p));
            condition.limit(personCount + nextInt);
            rows = Db.selectListByQuery("sf_jxzy_person_new", condition);
            if (currentShift.shiftType().equals("1")) {
                rows.add(getPerson("150622B001200020009202433"));
            }
            if (currentShift.shiftType().equals("2")) {
                rows.add(getPerson("150622B001200020009201312"));
            }
            if (currentShift.shiftType().equals("3")) {
                rows.add(getPerson("150622B001200020009201305"));
            }
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    private Row getPerson(String personCode) {
        Row row;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            QueryWrapper condition = new QueryWrapper()
                    .select("id", "person_code", "person_name")
                    .eq("person_code", personCode);
            row = Db.selectOneByQuery("sf_jxzy_person_new", condition);
        } finally {
            DataSourceKey.clear();
        }
        return row;
    }

    public List<Row> getSubstationList() {
        String cachedData = redisService.getSensor(projectNum, SUBSTATION_CACHE_KEY);
        if (StringUtils.isNotEmpty(cachedData)) {
            try {
                List<Row> rows = JSON.parseArray(cachedData, Row.class);
                log.debug("从Redis缓存获取分站列表，项目: {}", projectNum);
                return rows;
            } catch (Exception e) {
                log.warn("解析Redis缓存的分站列表失败: {}", e.getMessage());
            }
        }
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("is_delete", "0");
            // rows = Db.selectListByMap("sf_jxzy_substation", map);
            QueryWrapper queryWrapper = new QueryWrapper()
                    .select("id", "station_num", "station_code", "station_name", "area_desc")
                    .where(map);
            queryWrapper.orderByUnSafely("CAST(station_num AS UNSIGNED) ASC");
            rows = Db.selectListByQuery("sf_jxzy_substation", queryWrapper);

            rows.sort((r1, r2) -> {
                try {
                    int num1 = Integer.parseInt(r1.getString("station_num"));
                    int num2 = Integer.parseInt(r2.getString("station_num"));
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    return r1.getString("station_num").compareTo(r2.getString("station_num"));
                }
            });
            redisService.storeSensor(projectNum, SUBSTATION_CACHE_KEY, rows, CACHE_TIMEOUT);
            log.debug("从数据库查询分站列表并缓存到Redis，项目: {}, 数量: {}", projectNum, rows.size());
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getAreaList() {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("is_delete", "0");
            rows = Db.selectListByMap("sf_jxzy_area", map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public static JobKey getJobKey(String resourceId) {
        return new JobKey(resourceId, RYSSInfo.class.getName());
    }

    public static TriggerKey getTriggerKey(String resourceId) {
        return new TriggerKey(resourceId, RYSSInfo.class.getName());
    }
}
