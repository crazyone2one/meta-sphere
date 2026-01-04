package com.master.meta.utils;

import com.master.meta.config.FileTransferConfiguration;
import com.master.meta.constants.SensorKGType;
import com.master.meta.constants.SensorMNType;
import com.mybatisflex.core.datasource.DataSourceKey;
import com.mybatisflex.core.row.Db;
import com.mybatisflex.core.row.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
@Component
public class SensorUtil {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    private final RedisService redisService;
    private final ScpUtils scpUtils;
    private static final Long TIMEOUT = 60 * 60 * 24L;

    public SensorUtil(FileTransferConfiguration fileTransferConfiguration, RedisService redisService) {
        this.redisService = redisService;
        this.scpUtils = new ScpUtils(fileTransferConfiguration);
    }

    public List<Row> getRainDefineList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave1");
            Map<String, Object> map = new LinkedHashMap<>();
            if (Boolean.TRUE.equals(deleted)) {
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
            redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
            return sensorList;
        }
    }

    public List<Row> getSensorFromRedis(String projectNum, SensorMNType sensorMNType, Boolean deleted) {
        String rainDefineInRedis = redisService.getSensor(projectNum, sensorMNType.getKey());
        if (rainDefineInRedis != null) {
            return JSON.parseArray(rainDefineInRedis, Row.class);
        } else {
            List<Row> sensorList = getRainDefineList(sensorMNType.getTableName(), deleted);
            redisService.storeSensor(projectNum, sensorMNType.getKey(), sensorList, TIMEOUT);
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
        return getRows(projectNum, deleted, sensorMNType.getKey(), sensorMNType.getTableName());
    }

    public List<Row> getCDSSSensorFromRedis(String projectNum, SensorKGType sensorKGType, Boolean deleted) {
        return getRows(projectNum, deleted, sensorKGType.getKey(), sensorKGType.getTableName());
    }

    private List<Row> getRows(String projectNum, Boolean deleted, String key, String tableName) {
        String sensorListInRedis = redisService.getSensor(projectNum, key);
        if (sensorListInRedis != null) {
            List<Row> rows = JSON.parseArray(sensorListInRedis, Row.class);
            if (rows.isEmpty()) {
                List<Row> sensorList = getCDSSList(tableName, deleted);
                redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
                return sensorList;
            }
            return rows;
        } else {
            List<Row> sensorList = getCDSSList(tableName, deleted);
            redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
            return sensorList;
        }
    }

    public List<Row> getCDSSList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave1");
            Map<String, Object> map = new LinkedHashMap<>();
            if (Boolean.TRUE.equals(deleted)) {
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
        try {
            File file = new File(filePath);
            // 检查父目录是否存在，不存在则创建
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean dirCreated = parentDir.mkdirs();
                if (!dirCreated) {
                    log.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                    return;
                }
            }
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
                if (!newFile) {
                    log.error("Failed to create file: {}", filePath);
                    return;
                }
            }
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write(content);
                log.info("{} created successfully", type);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public List<Row> getWkkList(String tableName, Boolean deleted, String dataSourceKey) {
        List<Row> rows;
        try {
            DataSourceKey.use(dataSourceKey);
            Map<String, Object> map = new LinkedHashMap<>();
            if (Boolean.TRUE.equals(deleted)) {
                map.put("deleted", "0");
            }
            rows = Db.selectListByMap(tableName, map);
        } finally {
            DataSourceKey.clear();
        }
        return rows;
    }

    public List<Row> getWkkFromRedis(String projectNum, String key, String tableName, Boolean deleted) {
        String defineInRedis = redisService.getSensor(projectNum, key);
        return Optional.ofNullable(defineInRedis).map(s -> JSON.parseArray(s, Row.class))
                .orElseGet(() -> {
                    List<Row> sensorList = new LinkedList<>();
                    if ("150622020001".equals(projectNum)) {
                        sensorList = getWkkList(tableName, deleted, "ds-slave150622020001");
                    } else if ("150622007792".equals(projectNum)) {
                        sensorList = getWkkList(tableName, deleted, "ds-slave150622007792");
                    }
                    if (sensorList.isEmpty()) {
                        return sensorList;
                    }
                    redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
                    return sensorList;
                });
    }

    public List<Row> getGnssFromRedis(String projectNum, String key, String tableName, Boolean deleted) {
        String defineInRedis = redisService.getSensor(projectNum, key);
        return Optional.ofNullable(defineInRedis).map(s -> JSON.parseArray(s, Row.class))
                .orElseGet(() -> {
                    List<Row> sensorList = new LinkedList<>();
                    if ("150622020001".equals(projectNum)) {
                        sensorList = getWkkList(tableName, deleted, "ds-slave150622020001");
                    } else if ("150622007792".equals(projectNum)) {
                        sensorList = getWkkList(tableName, deleted, "ds-slave150622007792");
                    }
                    if (sensorList.isEmpty()) {
                        return sensorList;
                    }
                    redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
                    return sensorList;
                });
    }
}
