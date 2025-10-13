package com.master.meta.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Created by 11's papa on 2025/10/13
 */
@Data
public class ScheduleDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "所属组织")
    private String organizationName;

    @Schema(description = "所属项目")
    private String projectName;

    @Schema(description = "项目id")
    private String projectId;

    @Schema(description = "组织id")
    private String organizationId;

    @Schema(description = "任务id")
    private String id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "资源Id")
    private String resourceId;

    @Schema(description = "资源业务id")
    private Long num;

    @Schema(description = "类型")
    private String resourceType;

    @Schema(description = "运行规则（cron表达式）")
    private String value;

    @Schema(description = "上次完成时间")
    private Long lastTime;

    @Schema(description = "下次执行时间")
    private Long nextTime;

    @Schema(description = "任务状态")
    private boolean enable;

    @Schema(description = "操作人")
    private String createUserId;

    @Schema(description = "操作人")
    private String createUser;

    @Schema(description = "操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "资源num")
    private String resourceNum;
}
