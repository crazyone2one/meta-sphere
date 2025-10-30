package com.master.meta.utils;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.query.FluxTable;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Component
public class InfluxDbUtils {

    @Resource
    InfluxDBClient influxDBClient;

    @Value("${influx.bucket:''}")
    private String bucket;

    public List<FluxTable> getData(String query) {
        String flux = "from(bucket:\"" + bucket + "\")" + query;
        return influxDBClient.getQueryApi().query(flux);
    }

    /**
     * 根据measurement和sensorId删除指定时间段的数据
     *
     * @param measurement 测量名称
     * @param sensorId    传感器ID
     * @param startTime   开始时间(包含)
     * @param endTime     结束时间(包含)
     */
    public void deleteDataByTimeRange(String measurement, String sensorId, OffsetDateTime startTime, OffsetDateTime endTime) {
        DeleteApi deleteApi = influxDBClient.getDeleteApi();
        DeletePredicateRequest request = new DeletePredicateRequest();
        // 构造删除条件，根据measurement和sensorId进行过滤
        String predicate = "_measurement=\"" + measurement + "\" AND send_id=\"" + sensorId + "\"";
        request.setPredicate(predicate);
        // 设置时间范围
        request.setStart(startTime);
        request.setStop(endTime);
        try {
            deleteApi.delete(request, bucket, "admin");
        } catch (Exception e) {
            throw new RuntimeException("删除InfluxDB数据失败: " + e.getMessage(), e);
        }
    }
}
