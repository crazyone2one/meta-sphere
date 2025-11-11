package com.master.meta.service;

import com.master.meta.dto.BasePageRequest;
import com.master.meta.dto.TableBatchProcessDTO;
import com.master.meta.dto.system.*;
import com.master.meta.entity.SystemUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 用户 服务层。
 *
 * @author 11's papa
 * @since 2025-10-11
 */
public interface SystemUserService extends IService<SystemUser> {

    UserDTO getUserInfo();

    UserDTO getUserDTO(String userId);

    UserDTO getUserDTOByKeyword(String keyword);

    UserBatchCreateResponse addUser(UserBatchCreateRequest request, String source, String operator);

    UserEditRequest updateUser(UserEditRequest request);

    void updateUser(SystemUser user);

    Page<UserTableResponse> pageUserTable(BasePageRequest request);

    TableBatchProcessResponse updateUserEnable(UserChangeEnableRequest request, String userName);

    TableBatchProcessResponse deleteUser(TableBatchProcessDTO request, String operatorId, String operatorName);

    int deleteUserById(String id);

    TableBatchProcessResponse resetPassword(TableBatchProcessDTO request, String currentUserId);
}
