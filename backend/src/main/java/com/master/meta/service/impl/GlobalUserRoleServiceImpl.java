package com.master.meta.service.impl;

import com.master.meta.config.PermissionCache;
import com.master.meta.constants.InternalUserRole;
import com.master.meta.constants.UserRoleScope;
import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.permission.PermissionDefinitionItem;
import com.master.meta.dto.permission.PermissionSettingUpdateRequest;
import com.master.meta.entity.UserRole;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.UserRolePermissionMapper;
import com.master.meta.service.BaseUserRolePermissionService;
import com.master.meta.service.BaseUserRoleRelationService;
import com.master.meta.service.GlobalUserRoleService;
import com.master.meta.utils.SessionUtils;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.master.meta.constants.InternalUserRole.MEMBER;
import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;
import static com.master.meta.handle.result.CommonResultCode.ADMIN_USER_ROLE_PERMISSION;
import static com.master.meta.handle.result.SystemResultCode.GLOBAL_USER_ROLE_EXIST;
import static com.master.meta.handle.result.SystemResultCode.GLOBAL_USER_ROLE_PERMISSION;

/**
 * @author Created by 11's papa on 2025/10/24
 */
@Service("globalUserRoleService")
public class GlobalUserRoleServiceImpl extends BaseUserRoleServiceImpl implements GlobalUserRoleService {


    public GlobalUserRoleServiceImpl(UserRolePermissionMapper userRolePermissionMapper,
                                     BaseUserRoleRelationService baseUserRoleRelationService,
                                     BaseUserRolePermissionService baseUserRolePermissionService,
                                     PermissionCache permissionCache) {
        super(userRolePermissionMapper, baseUserRoleRelationService, baseUserRolePermissionService, permissionCache);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRole add(UserRole userRole) {
        userRole.setInternal(false);
        userRole.setScopeId(UserRoleScope.GLOBAL);
        checkExist(userRole);
        return super.add(userRole);
    }

    @Override
    public List<UserRole> list() {
        List<UserRole> userRoles = queryChain().list();
        // 先按照类型排序，再按照创建时间排序
        userRoles.sort(Comparator.comparingInt(this::getTypeOrder)
                .thenComparingInt(item -> getInternal(item.getInternal()))
                .thenComparing(UserRole::getCreateTime));
        return userRoles;
    }

    private int getInternal(Boolean internal) {
        return BooleanUtils.isTrue(internal) ? 0 : 1;
    }

    private int getTypeOrder(UserRole userRole) {
        Map<String, Integer> typeOrderMap = new HashMap<>(3) {{
            put(UserRoleType.SYSTEM.name(), 1);
            put(UserRoleType.ORGANIZATION.name(), 2);
            put(UserRoleType.PROJECT.name(), 3);
        }};
        return typeOrderMap.getOrDefault(userRole.getType(), 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserRole update(UserRole userRole) {
        UserRole originUserRole = getWithCheck(userRole.getId());
        checkGlobalUserRole(originUserRole);
        checkInternalUserRole(originUserRole);
        userRole.setInternal(false);
        checkExist(userRole);
        mapper.update(userRole);
        return userRole;
    }

    @Override
    public UserRole getWithCheck(String id) {
        return checkResourceExist(mapper.selectOneById(id));
    }

    @Override
    public void checkGlobalUserRole(UserRole userRole) {
        if (!Objects.equals(userRole.getScopeId(), UserRoleScope.GLOBAL)) {
            throw new CustomException(GLOBAL_USER_ROLE_PERMISSION);
        }
    }

    @Override
    public void delete(String id) {
        UserRole userRole = getWithCheck(id);
        checkGlobalUserRole(userRole);
        super.delete(userRole, MEMBER.getValue(), SessionUtils.getCurrentUserId(), UserRoleScope.SYSTEM);
    }

    @Override
    public List<PermissionDefinitionItem> getPermissionSetting(String roleId) {
        UserRole userRole = getWithCheck(roleId);
        checkGlobalUserRole(userRole);
        return getPermissionSetting(userRole);
    }

    @Override
    public void updatePermissionSetting(PermissionSettingUpdateRequest request) {
        UserRole userRole = getWithCheck(request.getUserRoleId());
        checkGlobalUserRole(userRole);
        // 内置管理员级别用户组无法更改权限
        checkAdminUserRole(userRole);
        super.updatePermissionSetting(request);
    }

    private void checkAdminUserRole(UserRole userRole) {
        if (Strings.CS.equalsAny(userRole.getCode(), InternalUserRole.ADMIN.getValue(),
                InternalUserRole.ORG_ADMIN.getValue(), InternalUserRole.PROJECT_ADMIN.getValue())) {
            throw new CustomException(ADMIN_USER_ROLE_PERMISSION);
        }
    }

    private void checkExist(UserRole userRole) {
        if (StringUtils.isBlank(userRole.getName())) {
            return;
        }
        boolean exists = queryChain().where(USER_ROLE.NAME.eq(userRole.getName())
                        .and(USER_ROLE.SCOPE_ID.eq(UserRoleScope.GLOBAL)))
                .and(USER_ROLE.ID.ne(userRole.getId())).exists();
        if (exists) {
            throw new CustomException(GLOBAL_USER_ROLE_EXIST);
        }
    }
}
