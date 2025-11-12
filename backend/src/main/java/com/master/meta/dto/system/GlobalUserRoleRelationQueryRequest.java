package com.master.meta.dto.system;

import com.master.meta.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Created by 11's papa on 2025/11/11
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GlobalUserRoleRelationQueryRequest extends BasePageRequest {
    @NotBlank
    @Schema(description = "用户组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleId;
}
