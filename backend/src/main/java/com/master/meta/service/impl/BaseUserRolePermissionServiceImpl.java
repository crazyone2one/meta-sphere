package com.master.meta.service.impl;

import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.entity.UserRolePermission;
import com.master.meta.mapper.UserRolePermissionMapper;
import com.master.meta.service.BaseUserRolePermissionService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.UserRolePermissionTableDef.USER_ROLE_PERMISSION;

/**
 * 用户组权限 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@Service
public class BaseUserRolePermissionServiceImpl extends ServiceImpl<UserRolePermissionMapper, UserRolePermission> implements BaseUserRolePermissionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(String roleId) {
        QueryChain<UserRolePermission> permissionQueryChain = queryChain().where(USER_ROLE_PERMISSION.ROLE_ID.eq(roleId));
        mapper.deleteByQuery(permissionQueryChain);
    }

    @Override
    public Set<String> getPermissionIdSetByRoleId(String roleId) {
        return getByRoleId(roleId).stream()
                .map(UserRolePermission::getPermissionId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<UserRolePermission> getByRoleId(String roleId) {
        return queryChain().where(USER_ROLE_PERMISSION.ROLE_ID.eq(roleId)).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        List<PermissionSettingUpdateRequest.PermissionUpdateRequest> permissions = request.getPermissions();
        // 先删除 (排除内置基本信息用户组)
        QueryChain<UserRolePermission> permissionQueryChain = queryChain().where(USER_ROLE_PERMISSION.ROLE_ID.eq(request.getUserRoleId())
                .and(USER_ROLE_PERMISSION.PERMISSION_ID.ne("PROJECT_BASE_INFO:READ")));
        mapper.deleteByQuery(permissionQueryChain);
        // 再新增
        String groupId = request.getUserRoleId();
        permissions.forEach(permission -> {
            if (BooleanUtils.isTrue(permission.getEnable())) {
                String permissionId = permission.getId();
                UserRolePermission groupPermission = new UserRolePermission();
                groupPermission.setRoleId(groupId);
                groupPermission.setPermissionId(permissionId);
                mapper.insert(groupPermission);
            }
        });
    }
}
