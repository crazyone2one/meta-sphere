package com.master.meta.dto.system;

import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRoleRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/31
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class UserDTO extends SystemUser {
    private List<UserRole> userRoles = new ArrayList<>();
    private List<UserRoleRelation> userRoleRelations = new ArrayList<>();
    private List<UserRoleResourceDTO> userRolePermissions = new ArrayList<>();

    @Schema(description =  "其他平台对接信息", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private byte[] platformInfo;

    @Schema(description = "头像")
    private String avatar;
}
