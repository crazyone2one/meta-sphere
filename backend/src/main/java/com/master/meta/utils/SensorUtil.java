package com.master.meta.utils;

import com.master.meta.constants.SensorType;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
@Component
public class SensorUtil {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    private final RedisService redisService;

    public SensorUtil(RedisService redisService) {
        this.redisService = redisService;
    }

    public List<Row> getRainDefineList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave1");
            Map<String, Object> map = new LinkedHashMap<>();
            if (deleted) {
                map.put("deleted", "0");
            }
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getSensorFromRedis(String projectNum, String key, String tableName, Boolean deleted) {
        String rainDefineInRedis = redisService.getSensor(projectNum, key);
        if (rainDefineInRedis != null) {
            return JSON.parseArray(rainDefineInRedis, Row.class);
        } else {
            List<Row> sensorList = getRainDefineList(tableName, deleted);
            redisService.storeSensor(projectNum, key, sensorList, 60 * 60 * 24 * 7);
            return sensorList;
        }
    }

    /**
     * 获取CDSS传感器列表
     *
     * @param projectNum 项目编号
     * @param sensorType 传感器类型
     * @param deleted    是否删除
     * @return 传感器列表
     */
    public List<Row> getCDSSSensorFromRedis(String projectNum, SensorType sensorType, Boolean deleted) {
        String sensorListInRedis = redisService.getSensor(projectNum, sensorType.getKey());
        if (sensorListInRedis != null) {
            return JSON.parseArray(sensorListInRedis, Row.class);
        } else {
            List<Row> sensorList = getCDSSList(sensorType.getTableName(), deleted);
            redisService.storeSensor(projectNum, sensorType.getKey(), sensorList, 60 * 60 * 24 * 7);
            return sensorList;
        }
    }

    private List<Row> getCDSSList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave1");
            Map<String, Object> map = new LinkedHashMap<>();
            if (deleted) {
                map.put("is_delete", "0");
            }
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public void generateFile(String filePath, String content, String type) {
        if (activeProfile.equals("dev")) {
            log.info("{}", content);
            return;
        }
        FileWriter fw = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
            }
            fw = new FileWriter(filePath);
            fw.write(content);
            log.info("{} created successfully", type);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            try {
                assert fw != null;
                fw.close();
            } catch (Exception e) {
                log.error("", e);
            }
        }
    }
}
