package com.master.meta.utils;

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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Created by 11's papa on 2025/10/15
 */
@Slf4j
@Component
public class SensorUtil {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    private final RedisService redisService;
    ScpUtils scpUtils = new ScpUtils();
    private static final Long TIMEOUT = 60 * 60 * 24L;

    public SensorUtil(RedisService redisService) {
        this.redisService = redisService;
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
            if (!file.exists()) {
                boolean newFile = file.createNewFile();
            }
            try (FileWriter fw = new FileWriter(filePath)) {
                fw.write(content);
                log.info("{} created successfully", type);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * 上传文件到远程服务器
     *
     * @param filePath   本地文件路径
     * @param targetPath 远程服务器目标路径
     */
    public void uploadFile(String filePath, String targetPath) {
        if (activeProfile.equals("dev")) {
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("file not exists: {}", filePath);
            return;
        }
        try {
            scpUtils.initClient();
            // 连接远程服务器 todo 从配置文件中获取
            scpUtils.connect("172.16.2.15", 8841, "root", "zkah@123");
            // 上传文件
            // scpUtils.uploadFile("C:\\Users\\the2n\\Desktop\\150622B0012000200092_CDDY_20241006163722.txt", "/home/app/luna");
            scpUtils.uploadFile(filePath, targetPath);
            log.info("file transfer successfully");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            scpUtils.close();
        }
    }

    public List<Row> getWkkList(String tableName, Boolean deleted) {
        List<Row> rows;
        try {
            DataSourceKey.use("ds-slave2");
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
                    List<Row> sensorList = getWkkList(tableName, deleted);
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
                    List<Row> sensorList = getWkkList(tableName, deleted);
                    if (sensorList.isEmpty()) {
                        return sensorList;
                    }
                    redisService.storeSensor(projectNum, key, sensorList, TIMEOUT);
                    return sensorList;
                });
    }
}
