package com.master.meta.utils;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxTable;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
