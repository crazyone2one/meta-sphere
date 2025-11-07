package com.master.meta.service;

import com.master.meta.dto.system.UserExcludeOptionDTO;
import com.master.meta.dto.system.UserTableResponse;
import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRoleRelation;
import com.mybatisflex.core.service.IService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * 用户组关系 服务层。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
public interface BaseUserRoleRelationService extends IService<UserRoleRelation> {
    List<UserRoleRelation> getByRoleId(String roleId);

    void deleteByRoleId(String roleId);

    List<String> getUserIdByRoleId(String roleId);

    List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds);

    List<UserRoleRelation> selectByUserId(String id);

    void updateUserSystemGlobalRole(@Valid SystemUser user, @Valid @NotEmpty String operator, @Valid @NotEmpty List<String> roleList);

    Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(@Valid @NotEmpty List<String> userIdList);

    List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword);
}
