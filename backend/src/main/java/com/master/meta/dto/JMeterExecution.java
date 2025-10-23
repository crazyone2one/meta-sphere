package com.master.meta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 2025/10/23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JMeterExecution {
    private String taskId;
    private String jmxFile;
    private String jtlFile;
    private String reportDir;
    private String status; // PENDING, RUNNING, SUCCESS, FAILED
    private String error;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
