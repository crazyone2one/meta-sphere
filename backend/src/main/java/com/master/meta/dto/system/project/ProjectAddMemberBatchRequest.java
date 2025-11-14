package com.master.meta.dto.system.project;

import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/11/14
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ProjectAddMemberBatchRequest extends ProjectAddMemberRequest {
    @Schema(description = "项目ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<
            @NotBlank(message = "{project.id.not_blank}", groups = {Created.class, Updated.class})
                    String> projectIds;
}
