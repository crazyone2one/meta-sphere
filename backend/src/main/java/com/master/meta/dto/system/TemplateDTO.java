package com.master.meta.dto.system;

import com.master.meta.entity.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TemplateDTO extends Template {
    @Schema(description = "是否平台默认模板")
    private Boolean platformDefault;
    @Schema(description = "模板关联的自定义字段")
    List<TemplateCustomFieldDTO> customFields;
    @Schema(description = "系统字段配置")
    List<TemplateCustomFieldDTO> systemFields;
}
