package com.master.meta.utils;

import com.master.meta.config.FileTransferConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@Component
public class FileHelper {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    private final ScpUtils scpUtils;

    public FileHelper(ScpUtils scpUtils) {
        this.scpUtils = scpUtils;
    }
    public String filePath(String filePath, String projectNum, String directoryName, String fileName) {
        return filePath + projectNum + File.separator + directoryName + File.separator + fileName;
    }
    public void uploadFile(FileTransferConfiguration.SlaveConfig slaveConfig, String localPath, String targetPath) {
        if (activeProfile.equals("dev")) {
            return;
        }
        File file = new File(localPath);
        if (!file.exists()) {
            log.warn("file not exists: {}", localPath);
            return;
        }
        try {
            // 上传文件
            scpUtils.uploadFile(slaveConfig, localPath, targetPath);
            log.info("file transfer successfully");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            scpUtils.close();
        }
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
}
