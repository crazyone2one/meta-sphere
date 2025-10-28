package com.master.meta;

import com.master.meta.utils.DateFormatUtil;
import com.master.meta.utils.SensorUtil;
import com.mybatisflex.core.row.Row;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/27
 */
@SpringBootTest
public class DemoOneTest {
    private final static String END_FLAG = "||";
    LocalDateTime now = LocalDateTime.now(ZoneOffset.of("+8"));

    @Resource
    SensorUtil sensorUtil;

    @Test
    public void testJxqy() {
        List<Row> sfJxzySubstation = sensorUtil.getCDSSList("sf_jxzy_area", false);
        List<Row> unDeleteSensorList = sfJxzySubstation.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        String content = "150622004499;" + "测试项目;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                bodyContent(unDeleteSensorList, now) +
                END_FLAG;
        System.out.println(content);
    }

    @Test
    public void testRyjz() {
        List<Row> sfJxzySubstation = sensorUtil.getCDSSList("sf_jxzy_substation", false);
        List<Row> unDeleteSensorList = sfJxzySubstation.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        String content = "150622004499;" + "测试项目;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                ryjzContent(unDeleteSensorList, now) +
                END_FLAG;
        System.out.println(content);
    }

    @Test
    public void testRyxx() {
        List<Row> sfJxzySubstation = sensorUtil.getCDSSList("sf_jxzy_person_new", false);
        List<Row> unDeleteRows = sfJxzySubstation.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        String content =
                // 文件体
                ryxxContent(unDeleteRows, now) + END_FLAG;
        System.out.println(content);
    }
    @Test
    public void testAqjkFz() {
        List<Row> sfJxzySubstation = sensorUtil.getCDSSList("sf_aqjk_stationinfo", false);
        List<Row> unDeleteRows = sfJxzySubstation.stream().filter(row -> BooleanUtils.isFalse(row.getBoolean("is_delete"))).toList();
        String content ="150622004499;" + "测试项目;" + DateFormatUtil.localDateTime2StringStyle2(now) + "~" +
                // 文件体
                fzContent(unDeleteRows, now) + END_FLAG;
        System.out.println(content);
    }

    private String fzContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String tmp = "150622B001200020009200016";

        for (Row s : rows) {
            String stationCode = s.getString("station_code");
//            if (tmp.equals(stationCode)) {
//                continue;
//            }
            content.append(stationCode).append(";")
                    .append(s.getString("station_position")).append(";")
                    .append(s.getString("area_x")).append(";")
                    .append(s.getString("area_y")).append(";")
                    .append(s.getString("area_z")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
        }
        return content.toString();
    }

    private String ryxxContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String tmp = "150622B001200020009200880";

        for (Row s : rows) {
            String personCode = s.getString("person_code");
            if (tmp.equals(personCode)) {
                continue;
            }
            content.append(personCode).append(";")
                    .append(s.getString("person_name")).append(";")
                    .append(s.getString("id_number")).append(";")
                    .append(s.getString("person_work")).append(";")
                    .append(s.getString("person_post")).append(";")
                    .append(s.getString("work_place")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle3(s.getLocalDateTime("person_birth"))).append(";")
                    .append(s.getString("gender")).append(";")
                    .append(s.getString("blood_type")).append(";")
                    .append(s.getString("person_education")).append(";")
                    .append(s.getString("marital_status")).append(";")
                    .append(s.getString("telephone")).append(";")
                    .append(s.getString("phone_number")).append(";")
                    .append(s.getString("home_address")).append(";")
                    .append(s.getString("other1")).append(";")
                    .append(s.getString("other2")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle3(now)).append("~")
            ;
        }
        return content.toString();
    }

    private String ryjzContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String tmp = "150622B001200020009212CB";

        for (Row s : rows) {
            String stationCode = s.getString("station_code");
            if (tmp.equals(stationCode)) {
                continue;
            }
            content.append(stationCode).append(";")
                    .append(s.getString("station_name")).append(";")
                    .append(s.getString("area_x")).append(";")
                    .append(s.getString("area_y")).append(";")
                    .append("930.97;")
                    .append(s.getString("area_desc")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
        }
        return content.toString();
    }

    private String bodyContent(List<Row> rows, LocalDateTime now) {
        StringBuilder content = new StringBuilder();
        String tmp = "150623B001200010034400";

        for (Row s : rows) {
            String areaCode = s.getString("area_code");
            String areaType = s.getString("area_type");
            if (tmp.equals(areaCode)) {
                continue;
            }
            content.append(StringUtils.isNoneBlank(areaType) ? areaType : "其他区域").append(";")
                    .append(areaCode).append(";")
                    .append(s.getString("area_max_person")).append(";")
                    .append(s.getString("area_name")).append(";")
                    .append(DateFormatUtil.localDateTime2StringStyle2(now)).append("~")
            ;
        }
        return content.toString();
    }
}
