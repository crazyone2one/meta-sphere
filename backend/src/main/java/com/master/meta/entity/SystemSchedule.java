package com.master.meta.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 定时任务 实体类。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "定时任务")
@Table("system_schedule")
public class SystemSchedule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Schema(description = "")
    private String id;

    /**
     * qrtz UUID
     */
    @Schema(description = "qrtz UUID")
    private String key;

    /**
     * 执行类型 cron
     */
    @Schema(description = "执行类型 cron")
    private String type;

    /**
     * cron 表达式
     */
    @Schema(description = "cron 表达式")
    private String value;

    /**
     * Schedule Job Class Name
     */
    @Schema(description = "Schedule Job Class Name")
    private String job;

    @Schema(description = "文件类型：CDDY、CDSS")
    private String resourceType;

    /**
     * 是否开启
     */
    @Schema(description = "是否开启")
    private Boolean enable;

    /**
     * 资源ID，api_scenario ui_scenario load_test
     */
    @Schema(description = "资源ID，api_scenario ui_scenario load_test")
    private String resourceId;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;
    private String sensorType;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 项目ID
     */
    @Schema(description = "项目ID")
    private String projectId;

    /**
     * 名称
     */
    @Schema(description = "名称")
    private String name;

    /**
     * 配置
     */
    @Schema(description = "配置")
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long num;
    @Schema(description = "是否删除")
    private Boolean deleted;
}
