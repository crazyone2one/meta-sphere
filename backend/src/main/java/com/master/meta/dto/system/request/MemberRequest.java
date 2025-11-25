package com.master.meta.dto.system.request;

import com.master.meta.dto.BasePageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MemberRequest extends BasePageRequest {

    @Schema(description = "组织ID或项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sourceId;
}
