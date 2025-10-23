package com.master.meta.config;

import com.influxdb.LogLevel;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Configuration
public class InfluxdbConfiguration {
    @Value("${influx.url}")
    private String url;

    @Value("${influx.token}")
    private String token;

    @Value("${influx.org:''}")
    private String org;

    @Value("${influx.bucket:''}")
    private String bucket;


    @Bean
    public InfluxDBClient influxDBClient() {
        InfluxDBClient client = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        client.setLogLevel(LogLevel.NONE);
        return client;
    }
}
