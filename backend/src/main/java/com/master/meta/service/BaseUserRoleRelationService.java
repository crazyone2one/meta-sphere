package com.master.meta.service;

import com.mybatisflex.core.service.IService;
import com.master.meta.entity.UserRoleRelation;
import org.apache.commons.lang3.EnumUtils;

import java.util.List;

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
}
