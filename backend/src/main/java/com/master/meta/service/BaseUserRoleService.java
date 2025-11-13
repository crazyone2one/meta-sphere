package com.master.meta.service;

import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRoleRelation;
import com.mybatisflex.core.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

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

    List<UserRole> selectByUserRoleRelations(List<UserRoleRelation> userRoleRelations);

    void checkRoleIsGlobalAndHaveMember(@Valid @NotEmpty List<String> roleIdList, boolean isSystem);

    UserRole getWithCheck(String roleId);

    UserRole getWithCheckByCode(String roleCode);

    List<UserRole> listByCode(List<String> roleCodeList);
}
