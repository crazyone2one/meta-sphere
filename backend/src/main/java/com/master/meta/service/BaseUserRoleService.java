package com.master.meta.service;

import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.UserRole;

import java.util.List;

/**
 * 用户组 服务层。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
public interface BaseUserRoleService extends IService<UserRole> {
    UserRole add(UserRole userRole);

    UserRole checkResourceExist(UserRole userRole);

    void checkInternalUserRole(UserRole userRole);

    void delete(UserRole userRole, String defaultRoleId, String currentUserId, String orgId);

    List<PermissionDefinitionItem> getPermissionSetting(UserRole userRole);
}
