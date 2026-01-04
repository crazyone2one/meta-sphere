package com.master.meta.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "transfer")
public class FileTransferConfiguration {
    @Getter
    @Setter
    private List<Map<String, SlaveConfig>> slave;

    public SlaveConfig getSlaveConfigByResourceId(String resourceId) {
        Map<String, SlaveConfig> slaveMap = slave.stream()
                .flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> replacement // 如果key重复，使用新的值
                ));
        return slaveMap.get(resourceId);
    }

    @Data
    public static class SlaveConfig {
        private String host;
        private Integer port;
        private String username;
        private String password;
        private String localPath;
        private String remotePath;
    }
}
