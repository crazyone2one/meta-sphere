package com.master.meta.entity;

import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;

import java.io.Serial;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目应用 实体类。
 *
 * @author 11's papa
 * @since 2025-11-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "项目应用")
@Table("project_application")
public class ProjectApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Id
    @Schema(description = "ID")
    private String id;

    @Schema(description =  "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_application.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project_application.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description =  "配置项", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project_application.type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{project_application.type.length_range}", groups = {Created.class, Updated.class})
    private String type;

    /**
     * 配置值
     */
    @Schema(description = "配置值")
    private String typeValue;

}
