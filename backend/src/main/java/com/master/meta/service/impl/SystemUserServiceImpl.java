package com.master.meta.service.impl;

import com.master.meta.dto.UserInfoDTO;
import com.master.meta.entity.SystemUser;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.service.SystemUserService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;

/**
 * 用户 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser> implements SystemUserService {

    @Override
    public UserInfoDTO getUserInfo() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return queryChain().where(SYSTEM_USER.NAME.eq(name)).oneAs(UserInfoDTO.class);
    }
}
