package com.master.meta.service;

import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.entity.UserRole;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/24
 */
public interface GlobalUserRoleService extends BaseUserRoleService {
    UserRole add(UserRole userRole);

    UserRole update(UserRole userRole);

    UserRole getWithCheck(String id);

    void checkGlobalUserRole(UserRole userRole);

    void delete(String id);

    List<PermissionDefinitionItem> getPermissionSetting(String roleId);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);
}
