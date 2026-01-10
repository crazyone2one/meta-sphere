package com.master.meta.schedule;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.handle.schedule.BaseScheduleJob;
import com.master.meta.service.SensorService;
import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.FileHelper;
import com.master.meta.utils.RandomUtil;
import com.master.meta.utils.ShiftUtils;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.val;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class RYSSInfo extends BaseScheduleJob {

    private RYSSInfo(SensorService sensorService, FileTransferConfiguration fileTransferConfiguration, FileHelper fileHelper) {
        super(sensorService, fileHelper, fileTransferConfiguration);
    }

    @Override
    protected void businessExecute(JobExecutionContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));
        val personList = getPersonList();
        val substationList = getSubstationList();
        val areaList = getAreaList();
        FileTransferConfiguration.SlaveConfig slaveConfig = slaveConfig();
        String fileName = "150622B0012000200092_RYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        if (config.isFmFlag()) {
            fileName = projectNum + "_NRYSS_" + DateFormatUtil.localDateTimeToString(now) + ".txt";
        }
        String headerContent = projectNum + ";" + projectName + ";" + DateFormatUtil.localDateTime2StringStyle2(now) + "^";
        String content =
                (config.isFmFlag() ? headerContent : "") +
                        // 文件体
                        bodyContent(personList, substationList, areaList, now) +
                        (config.isFmFlag() ? "]]]" : END_FLAG);
        // String filePath = "/app/files/rydw/" + fileName;
        String filePath = fileHelper.filePath(slaveConfig.getLocalPath(), projectNum, "rydw", fileName);
        fileHelper.generateFile(filePath, content, "实时数据[" + fileName + "]");
        fileHelper.uploadFile(slaveConfig, filePath, slaveConfig.getRemotePath() + "rydw");
    }

    private String bodyContent(List<Row> personList, List<Row> substationList, List<Row> areaList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        if (!config.isFmFlag()) {
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";309;150622B001200020009201307/高洪方;~");
        }
//        personList = personList.subList(0, 10);
        List<ShiftUtils.ShiftPeriod> shifts = ShiftUtils.createStandardThreeShifts();
        // 当前所在班次
        ShiftUtils.ShiftPeriod currentShift = ShiftUtils.getCurrentShift(now, shifts);
        // val inDateTmp = config.getField("inDate", String.class);
        val inDate = ShiftUtils.getShiftStartDateTime(currentShift, now);
        LocalDateTime shiftEndTime = ShiftUtils.getShiftEndDateTime(currentShift, now);
        personList.forEach(person -> {
            val substation = RandomUtil.getRandomSubList(substationList, 1).getFirst();
            val area = RandomUtil.getRandomSubList(areaList, 1).getFirst();

            content.append(person.getString("person_code")).append(";");
            content.append(person.getString("person_name")).append(";");
            content.append(now.isEqual(shiftEndTime) ? "2" : "1").append(";").append(DateFormatUtil.localDateTime2StringStyle2(inDate)).append(";");
            String outTime = config.isFmFlag() ? "" : "xxxx-xx-xx xx:xx:xx";
            if (now.isEqual(shiftEndTime)) {
                outTime = DateFormatUtil.localDateTime2StringStyle2(shiftEndTime);
            }
            content.append(outTime).append(";");
            content.append(area.getString(config.isFmFlag() ? "area_type" : "area_code")).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
            content.append(substation.getString("station_code")).append(";");
            content.append(DateFormatUtil.localDateTime2StringStyle2(now)).append(";");
            if (!config.isFmFlag()) {
                // content.append("默认班制;16.21;正常;0;0;");
                content.append(behavior(substationList, now));
            } else {
                content.append("1;1;10;1;");
                content.append(";;;");
                content.append("0;0;1;");
                content.append(";;");
            }

            content.append(config.isFmFlag() ? "^" : "~");
        });
        return content.toString();
    }

    private String behavior(List<Row> substationList, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        // Create a mutable copy of substationList for this execution
        List<Row> availableSubstations = new ArrayList<>(substationList);
        Random random = new Random();
        val nextInt = random.nextInt(5, 9);

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
        return content.toString();
    }

    public List<Row> getPersonList() {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave" + projectNum);
            QueryWrapper condition = new QueryWrapper()
                    .select("id", "person_code", "person_name")

                    .ne("id_number", "")
                    .notLike("person_name", "厂家")
                    .notLike("person_name", "贵宾")
                    .notLike("person_name", "华电")
                    .notLike("person_name", "测试").limit(100);
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
