package com.master.meta.dto.system.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserVO {
    private String id;
    private String name;
    @Schema(description = "组织ID")
    private String sourceId;
}
