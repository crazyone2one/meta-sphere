package com.master.meta.service;

import com.master.meta.dto.JMeterExecution;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Slf4j
@Service
public class JMeterService {
    @Value("${jmeter.home}")
    private String jmeterHome;
    @Value("${jmeter.results.dir:./results}")
    private String resultsDir;
    @Value("${jmeter.reports.dir:./reports}")
    private String reportsDir;
    // 存储正在运行的任务，防止重复执行
    private final Map<String, Process> runningTasks = new ConcurrentHashMap<>();

    public JMeterExecution execute(String jmxFile, Map<String, String> variables) {
        String taskId = generateTaskId();
        Path jmxPath = Paths.get("E:\\jmeter\\nm2.0", jmxFile);
        // 验证 JMX 文件存在
        if (!Files.exists(jmxPath)) {
            return buildFailedExecution(taskId, jmxFile, "JMX file not found: " + jmxFile);
        }
        // 创建结果目录
        createDirectories();

        String jtlFile = resultsDir + "/result_" + taskId + ".jtl";
        String reportDir = reportsDir + "/report_" + taskId;

        List<String> command = buildCommand(jmxPath.toString(), jtlFile, reportDir, variables);

        log.info("Executing JMeter test: {}", command);
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并错误流

            Process process = pb.start();
            runningTasks.put(taskId, process);

            // 异步读取日志
            readProcessOutput(process, taskId);

            // 等待完成（生产环境建议异步 + 回调）
            int exitCode = process.waitFor();
            runningTasks.remove(taskId);

            if (exitCode == 0) {
                // 生成 HTML 报告
                generateHtmlReport(jtlFile, reportDir);
                return buildSuccessExecution(taskId, jmxFile, jtlFile, reportDir);
            } else {
                return buildFailedExecution(taskId, jmxFile, "JMeter exited with code: " + exitCode);
            }

        } catch (Exception e) {
            log.error("Failed to execute JMeter test", e);
            runningTasks.remove(taskId);
            return buildFailedExecution(taskId, jmxFile, e.getMessage());
        }
    }

    private String generateTaskId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private JMeterExecution buildFailedExecution(String taskId, String jmxFile, String error) {
        return JMeterExecution.builder()
                .taskId(taskId)
                .jmxFile(jmxFile)
                .status("FAILED")
                .error(error)
                .startTime(LocalDateTime.now())
                .build();
    }

    private JMeterExecution buildRunningExecution(String taskId) {
        return JMeterExecution.builder()
                .taskId(taskId)
                .status("RUNNING")
                .startTime(LocalDateTime.now())
                .build();
    }

    private JMeterExecution buildSuccessExecution(String taskId, String jmxFile, String jtlFile, String reportDir) {
        return JMeterExecution.builder()
                .taskId(taskId)
                .jmxFile(jmxFile)
                .jtlFile(jtlFile)
                .reportDir(reportDir)
                .status("SUCCESS")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    private void readProcessOutput(Process process, String taskId) {
        new Thread(() -> {
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("[JMeter-{}]: {}", taskId, line);
                }
            } catch (IOException e) {
                log.error("Error reading JMeter output", e);
            }
        }).start();
    }

    private void generateHtmlReport(String jtlFile, String reportDir) throws IOException, InterruptedException {
        List<String> cmd = Arrays.asList(
                jmeterHome + "/bin/jmeter.sh",
                "-g", jtlFile,
                "-e",
                "-o", reportDir
        );
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            log.warn("Failed to generate HTML report for {}", jtlFile);
        }
    }

    private List<String> buildCommand(String jmxFile, String jtlFile, String reportDir, Map<String, String> variables) {
        List<String> cmd = new ArrayList<>();
        cmd.add(jmeterHome + "/bin/jmeter.bat"); // Windows 使用 jmeter.bat
        cmd.add("-n"); // 非 GUI 模式
        cmd.add("-t"); // 测试计划
        cmd.add(jmxFile);
        cmd.add("-l"); // 结果文件
        cmd.add(jtlFile);
        cmd.add("-e"); // 生成报告
        cmd.add("-o");
        cmd.add(reportDir);

        // 注入变量
        variables.forEach((key, value) -> {
            cmd.add("-J" + key + "=" + value);
        });

        return cmd;
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(resultsDir));
            Files.createDirectories(Paths.get(reportsDir));
        } catch (IOException e) {
            log.error("Failed to create directories", e);
        }
    }

    public JMeterExecution getExecutionStatus(String taskId) {
        return runningTasks.containsKey(taskId) ?
                buildRunningExecution(taskId) :
                null; // 实际项目中应从数据库查询历史记录
    }
}
