package com.master.meta.service;

import com.master.meta.dto.UserInfoDTO;
import com.mybatisflex.core.service.IService;
import com.master.meta.entity.SystemUser;

/**
 * 用户 服务层。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
public interface SystemUserService extends IService<SystemUser> {

    UserInfoDTO getUserInfo();
}
