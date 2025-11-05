package com.master.meta.handle.security;

import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRoleRelation;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Created by 11's papa on 2025/10/11
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return QueryChain.of(SystemUser.class).where(SystemUser::getName).eq(username).oneOpt()
                .map(user -> {
                    // 查询用户关联的权限数据
                    List<String> rolesCodes = QueryChain.of(UserRoleRelation.class)
                            .where(UserRoleRelation::getUserId).eq(user.getId()).list()
                            .stream().map(UserRoleRelation::getRoleCode).toList();
                    return new CustomUserDetails(user, rolesCodes);
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
