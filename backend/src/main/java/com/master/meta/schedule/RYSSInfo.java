package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileManager;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.ShiftUtils;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class RYSSInfo extends BaseScheduleJob {

    private RYSSInfo(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileManager fileManager) {
        super(sensorService, fileManager, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        val substationList = getSubstationList();
        val areaList = getAreaList();
        List<ShiftUtils.ShiftPeriod> shifts = ShiftUtils.createStandardThreeShifts();
        // 当前所在班次
        ShiftUtils.ShiftPeriod currentShift = ShiftUtils.getCurrentShift(now, shifts);
        List<Row> personList = getPersonList(currentShift);
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String fileName = "150622B0012000200092_RYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        if (config.isFmFlag()) {
            fileName = projectNum + "_NRYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        }
        String headerContent = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "^";
        String content =
                (config.isFmFlag() ? headerContent : "") +
                        // 文件体
                        bodyContent(substationList, areaList, now, personList, currentShift) +
                        (config.isFmFlag() ? "]]]" : END_FLAG);
        // String filePath = "/app/files/rydw/" + fileName;
        String filePath = fileManager.buildFilePath(slaveConfig.getLocalPath(), projectNum, "rydw", fileName);
        fileManager.writeToFile(filePath, content, "实时数据[" + fileName + "]");
        fileManager.uploadAndCleanup(slaveConfig, filePath, slaveConfig.getRemotePath() + "rydw");
        // 在文件处理完成后执行删除操作
        deletePersonBehaviorAtShiftEnd(now);
    }

    private void deletePersonBehaviorAtShiftEnd(LocalDateTime now) {
        List<ShiftUtils.ShiftPeriod> shifts = ShiftUtils.createStandardThreeShifts();

        // 首先尝试获取即将结束的班次（处理班次交接点的情况）
        ShiftUtils.ShiftPeriod endingShift = ShiftUtils.getEndingShiftAtTime(now, shifts);

        if (endingShift != null) {
            // 在班次交接点，删除即将结束班次的人员数据
            List<Row> personList = getPersonList(endingShift);
            for (Row person : personList) {
                String personCode = person.getString("person_code");
                sensorService.deletePersonBehavior(personCode);
            }
        } else {
            // 非交接点，使用原有逻辑
            ShiftUtils.ShiftPeriod currentShift = ShiftUtils.getCurrentShift(now, shifts);
            if (currentShift != null && ShiftUtils.isCurrentTimeEqualShiftEndTime(now, currentShift)) {
                List<Row> personList = getPersonList(currentShift);
                for (Row person : personList) {
                    String personCode = person.getString("person_code");
                    sensorService.deletePersonBehavior(personCode);
                }
            }
        }
    }

    private String bodyContent(List<Row> substationList, List<Row> areaList, LocalDateTime now
            , List<Row> personList, ShiftUtils.ShiftPeriod currentShift) {
        StringBuilder content = new StringBuilder();
        if (!config.isFmFlag()) {
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";309;150622B001200020009201307/高洪方;~");
        }
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
            Row substation = RandomUtil.getRandomSubList(substationList, 1).getFirst();
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
                content.append(behavior(substationList, now, personCode));
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
        }
        return content.toString();
    }

    private String behavior(List<Row> substationList, LocalDateTime now, String personCode) {
        StringBuilder content = new StringBuilder();
        String personBehavior = sensorService.getPersonBehavior(personCode);
        if (StringUtils.isNotEmpty(personBehavior)) {
            content.append(personBehavior).append(",");
        }
        // Create a mutable copy of substationList for this execution
        List<Row> availableSubstations = new ArrayList<>(substationList);
        Random random = new Random();
        val nextInt = random.nextInt(1, 3);
        // int nextInt = now.getMinute() - shiftBeginTime.getMinute();
        for (int i = 0; i < nextInt; i++) {
            Row substation;
            if (!availableSubstations.isEmpty()) {
                // Get random index and remove the item
                // 生成随机索引（范围：0 ~ 列表长度-1）
                int randomIndex = random.nextInt(substationList.size());
                substation = availableSubstations.get(randomIndex);
            } else {
                // Reset list if all items have been used
                availableSubstations = new ArrayList<>(substationList);
                int randomIndex = random.nextInt(substationList.size());
                substation = availableSubstations.get(randomIndex);
            }
            content.append(substation.getString("station_code")).append("&")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now.minusMinutes(nextInt - (i + 1L))))
                    .append(",");
        }
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
                    .notLike("person_post", "兖矿")
                    .limit(personCount + nextInt);
            rows = Db.selectListByQuery("sf_jxzy_person_new", condition);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getSubstationList() {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("is_delete", "0");
            rows = Db.selectListByMap("sf_jxzy_substation", map);
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
