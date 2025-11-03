package com.master.meta.dto.system;

import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/31
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserTableResponse extends SystemUser {
    @Schema(description = "用户所属用户组")
    private List<UserRole> userRoleList = new ArrayList<>();

    public void setUserRole(UserRole userRole) {
        if (!userRoleList.contains(userRole)) {
            userRoleList.add(userRole);
        }
    }
}
