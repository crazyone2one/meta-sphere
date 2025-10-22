package com.master.meta.utils;

import com.master.meta.constants.SensorKGType;
import com.master.meta.constants.SensorMNType;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
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

    public List<Row> getSensorFromRedis(String projectNum, SensorMNType sensorMNType, Boolean deleted) {
        String rainDefineInRedis = redisService.getSensor(projectNum, sensorMNType.getKey());
        if (rainDefineInRedis != null) {
            return JSON.parseArray(rainDefineInRedis, Row.class);
        } else {
            List<Row> sensorList = getRainDefineList(sensorMNType.getTableName(), deleted);
            redisService.storeSensor(projectNum, sensorMNType.getKey(), sensorList, 60 * 60 * 24 * 7);
            return sensorList;
        }
    }

    /**
     * 获取CDSS传感器列表
     *
     * @param projectNum   项目编号
     * @param sensorMNType 传感器类型
     * @param deleted      是否删除
     * @return 传感器列表
     */
    public List<Row> getCDSSSensorFromRedis(String projectNum, SensorMNType sensorMNType, Boolean deleted) {
        String sensorListInRedis = redisService.getSensor(projectNum, sensorMNType.getKey());
        if (sensorListInRedis != null) {
            return JSON.parseArray(sensorListInRedis, Row.class);
        } else {
            List<Row> sensorList = getCDSSList(sensorMNType.getTableName(), deleted);
            redisService.storeSensor(projectNum, sensorMNType.getKey(), sensorList, 60 * 60 * 24 * 7);
            return sensorList;
        }
    }

    public List<Row> getCDSSSensorFromRedis(String projectNum, SensorKGType sensorKGType, Boolean deleted) {
        String sensorListInRedis = redisService.getSensor(projectNum, sensorKGType.getKey());
        if (sensorListInRedis != null) {
            return JSON.parseArray(sensorListInRedis, Row.class);
        } else {
            List<Row> sensorList = getCDSSList(sensorKGType.getTableName(), deleted);
            redisService.storeSensor(projectNum, sensorKGType.getKey(), sensorList, 60 * 60 * 24 * 7);
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
