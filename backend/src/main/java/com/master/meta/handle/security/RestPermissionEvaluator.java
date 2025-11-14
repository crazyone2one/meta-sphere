package com.master.meta.handle.security;

import com.master.meta.entity.UserRolePermission;
import com.mybatisflex.core.query.QueryChain;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Created by 11's papa on 2025/11/4
 */
@Service("rpe")
public class RestPermissionEvaluator {
    private static final String SUPER_ADMIN = "admin";

    /**
     * 检查用户是否有指定权限
     *
     * @param permission 权限标识
     * @return 是否有权限
     */
    public boolean hasPermission(String permission) {
        if (StringUtils.isEmpty(permission)) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> roles = authorities.stream().map(GrantedAuthority::getAuthority).toList();
        if (roles.contains(SUPER_ADMIN)) {
            return true;
        }
        List<String> permissions = QueryChain.of(UserRolePermission.class)
                .where(UserRolePermission::getRoleCode).in(roles).list()
                .stream()
                .map(UserRolePermission::getPermissionId).toList();
        return permissions.stream().anyMatch(permission::contains);
    }

    /**
     * 检查用户是否具有任意一个指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否有任一权限
     */
    public boolean hasAnyPermission(String[] permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查用户是否具有所有指定权限
     *
     * @param permissions 权限标识数组
     * @return 是否有所有权限
     */
    public boolean hasAllPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }
}
