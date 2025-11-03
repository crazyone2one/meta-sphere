package com.master.meta.dto.system;

import com.master.meta.dto.permission.UserRoleResource;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRolePermission;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/31
 */
@Data
public class UserRoleResourceDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private UserRoleResource resource;
    private List<UserRolePermission> permissions;
    private String type;

    private UserRole userRole;
    private List<UserRolePermission> userRolePermissions;
}

