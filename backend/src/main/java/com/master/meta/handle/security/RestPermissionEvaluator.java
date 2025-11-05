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

/**
 * @author Created by 11's papa on 2025/11/4
 */
@Service("rpe")
public class RestPermissionEvaluator {
    private static final String SUPER_ADMIN = "admin";

    public boolean hasPermission(String permission) {
        if (StringUtils.isNoneBlank(permission)) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
}
