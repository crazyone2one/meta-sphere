package com.master.meta.service;

import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.handle.validation.Created;
import com.master.meta.handle.validation.Updated;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.UserRolePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/**
 * 用户组权限 服务层。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
public interface BaseUserRolePermissionService extends IService<UserRolePermission> {
    void deleteByRoleId(String roleId);

    Set<String> getPermissionIdSetByRoleId(String roleId);

    List<UserRolePermission> getByRoleId(String roleId);

    void updatePermissionSetting(PermissionSettingUpdateRequest request);
}
