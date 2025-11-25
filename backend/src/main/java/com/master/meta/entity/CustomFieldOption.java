package com.master.meta.entity;

import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;

import java.io.Serializable;

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
 * 自定义字段选项 实体类。
 *
 * @author 11's papa
 * @since 2025-11-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自定义字段选项")
@Table("custom_field_option")
public class CustomFieldOption implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 自定义字段ID
     */
    @Id
    @Schema(description = "自定义字段ID")
    private String id;

    @Schema(description = "自定义字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.field_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_option.field_id.length_range}", groups = {Created.class, Updated.class})
    private String fieldId;

    @Schema(description = "选项值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.value.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{custom_field_option.value.length_range}", groups = {Created.class, Updated.class})
    private String value;

    @Schema(description = "选项值名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{custom_field_option.text.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{custom_field_option.text.length_range}", groups = {Created.class, Updated.class})
    private String text;

    @Schema(description = "是否内置", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{custom_field_option.internal.not_blank}", groups = {Created.class})
    private Boolean internal;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{custom_field_option.pos.not_blank}", groups = {Created.class})
    private Integer pos;

}
