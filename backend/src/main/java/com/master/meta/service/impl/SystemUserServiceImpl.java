package com.master.meta.service.impl;

import com.master.meta.handle.security.CustomUserDetails;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.master.meta.entity.SystemUser;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.service.SystemUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>  implements SystemUserService{

    @Override
    public SystemUser getUserInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        CustomUserDetails principal = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.user();
    }
}
