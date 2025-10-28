package com.master.meta.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Data
public class BaseCondition {
    @Schema(description = "关键字")
    private String keyword;
    private String projectId;
    private String sensorType;
    private String sensorGroup;
}
