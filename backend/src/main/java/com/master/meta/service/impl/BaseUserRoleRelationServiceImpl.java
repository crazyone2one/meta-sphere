package com.master.meta.service.impl;

import com.master.meta.constants.InternalUserRole;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.BaseUserRoleRelationService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.handle.result.CommonResultCode.USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION;

/**
 * 用户组关系 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@Service
public class BaseUserRoleRelationServiceImpl extends ServiceImpl<UserRoleRelationMapper, UserRoleRelation> implements BaseUserRoleRelationService {

    @Override
    public List<UserRoleRelation> getByRoleId(String roleId) {
        return queryChain().where(USER_ROLE_RELATION.ROLE_ID.eq(roleId)).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(String roleId) {
        List<UserRoleRelation> userRoleRelations = getByRoleId(roleId);
        userRoleRelations.forEach(userRoleRelation ->
                checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleId()));
        QueryChain<UserRoleRelation> userRoleRelationQueryChain = queryChain().where(USER_ROLE_RELATION.ROLE_ID.eq(roleId));
        mapper.deleteByQuery(userRoleRelationQueryChain);
    }

    @Override
    public List<String> getUserIdByRoleId(String roleId) {
        return queryChain().select(USER_ROLE_RELATION.USER_ID).where(USER_ROLE_RELATION.ROLE_ID.eq(roleId)).listAs(String.class);
    }

    @Override
    public List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return queryChain()
                .select(USER_ROLE_RELATION.ROLE_ID, USER_ROLE_RELATION.USER_ID, USER_ROLE_RELATION.SOURCE_ID)
                .where(USER_ROLE_RELATION.USER_ID.in(userIds)).list();
    }

    private void checkAdminPermissionRemove(String userId, String roleId) {
        if (Objects.equals(roleId, InternalUserRole.ADMIN.getValue()) && Objects.equals(userId, InternalUserRole.ADMIN.getValue())) {
            throw new CustomException(USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }
}
