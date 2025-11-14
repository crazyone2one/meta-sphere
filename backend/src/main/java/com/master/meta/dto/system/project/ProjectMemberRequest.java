package com.master.meta.dto.system.project;

import com.master.meta.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 2025/11/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ProjectMemberRequest extends BasePageRequest {
    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{project.id.not_blank}")
    private String projectId;
}
