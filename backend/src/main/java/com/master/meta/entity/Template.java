package com.master.meta.entity;

import com.master.meta.constants.TemplateScene;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.EnumValue;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模版 实体类。
 *
 * @author 11's papa
 * @since 2025-11-20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模版")
@Table("template")
public class Template implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{template.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{template.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否是内置模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{template.internal.not_blank}", groups = {Created.class})
    private Boolean internal;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime updateTime;

    /**
     * 创建时间
     */
    @Column(onInsertValue = "now()")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createUser;

    @Schema(description = "组织或项目级别字段（PROJECT, ORGANIZATION）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scope_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template.scope_type.length_range}", groups = {Created.class, Updated.class})
    private String scopeType;

    @Schema(description = "组织或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scope_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{template.scope_id.length_range}", groups = {Created.class, Updated.class})
    private String scopeId;

    @Schema(description = "是否开启api字段名配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{template.enable_third_part.not_blank}", groups = {Created.class})
    private Boolean enableThirdPart;

    /**
     * 项目模板所关联的组织模板ID
     */
    @Schema(description = "项目模板所关联的组织模板ID")
    private String refId;

    @Schema(description = "使用场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{template.scene.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 30, message = "{template.scene.length_range}", groups = {Created.class, Updated.class})
//    private String scene;
    @EnumValue
    private TemplateScene scene;


    @Schema(description = "是否是默认模板")
    @Column(ignore = true)
    private Boolean enableDefault = false;

    @Schema(description = "是否是平台自动获取模板")
    @Column(ignore = true)
    private Boolean enablePlatformDefault = false;
}
