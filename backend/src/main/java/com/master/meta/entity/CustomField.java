package com.master.meta.entity;

import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 自定义字段 实体类。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义字段")
@Table("custom_field")
public class CustomField implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自定义字段ID
     */
    @Id
    @Schema(description = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{custom_field.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "自定义字段名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_field.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(description = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.scene.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{custom_field.scene.length_range}", groups = {Created.class, Updated.class})
    private String scene;

    @Schema(description = "自定义字段类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{custom_field.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    /**
     * 自定义字段备注
     */
    @Schema(description = "自定义字段备注")
    private String remark;

    @Schema(description = "是否是内置字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{custom_field.internal.not_blank}", groups = {Created.class})
    private Boolean internal;

    @Schema(description = "组织或项目级别字段（PROJECT, ORGANIZATION）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.scope_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field.scope_type.length_range}", groups = {Created.class, Updated.class})
    private String scopeType;

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
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    /**
     * 项目字段所关联的组织字段ID
     */
    @Schema(description = "项目字段所关联的组织字段ID")
    private String refId;

    /**
     * 是否需要手动输入选项key
     */
    @Schema(description = "是否需要手动输入选项key")
    private Boolean enableOptionKey;

    @Schema(description = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

}
