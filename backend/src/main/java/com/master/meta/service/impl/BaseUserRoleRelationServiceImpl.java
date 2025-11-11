package com.master.meta.service.impl;

import com.master.meta.constants.InternalUserRole;
import com.master.meta.constants.UserRoleScope;
import com.master.meta.dto.system.UserExcludeOptionDTO;
import com.master.meta.dto.system.UserTableResponse;
import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.BaseUserRoleRelationService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;
import static com.master.meta.handle.result.CommonResultCode.USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION;

/**
 * 用户组关系 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-24
 */
@Service("baseUserRoleRelationService")
public class BaseUserRoleRelationServiceImpl extends ServiceImpl<UserRoleRelationMapper, UserRoleRelation> implements BaseUserRoleRelationService {

    @Override
    public List<UserRoleRelation> getByRoleId(String roleId) {
        return queryChain().where(USER_ROLE_RELATION.ROLE_CODE.eq(roleId)).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByRoleId(String roleId) {
        List<UserRoleRelation> userRoleRelations = getByRoleId(roleId);
        userRoleRelations.forEach(userRoleRelation ->
                checkAdminPermissionRemove(userRoleRelation.getUserId(), userRoleRelation.getRoleCode()));
        QueryChain<UserRoleRelation> userRoleRelationQueryChain = queryChain().where(USER_ROLE_RELATION.ROLE_CODE.eq(roleId));
        mapper.deleteByQuery(userRoleRelationQueryChain);
    }

    @Override
    public List<String> getUserIdByRoleId(String roleId) {
        return queryChain().select(USER_ROLE_RELATION.USER_ID).where(USER_ROLE_RELATION.ROLE_CODE.eq(roleId)).listAs(String.class);
    }

    @Override
    public List<UserRoleRelation> getUserIdAndSourceIdByUserIds(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return List.of();
        }
        return queryChain()
                .select(USER_ROLE_RELATION.ROLE_CODE, USER_ROLE_RELATION.USER_ID, USER_ROLE_RELATION.SOURCE_ID)
                .where(USER_ROLE_RELATION.USER_ID.in(userIds)).list();
    }

    @Override
    public List<UserRoleRelation> selectByUserId(String id) {
        return queryChain().where(USER_ROLE_RELATION.USER_ID.eq(id)).list();
    }

    @Override
    public void updateUserSystemGlobalRole(SystemUser user, String operator, List<String> roleList) {
        //更新用户权限
        List<String> deleteRoleList = new ArrayList<>();
        List<UserRoleRelation> saveList = new ArrayList<>();
        List<UserRoleRelation> userRoleRelationList = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(user.getId())).list();
        List<String> userSavedRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleCode).toList();
        //获取要移除的权限
        for (String userSavedRoleId : userSavedRoleIdList) {
            if (!roleList.contains(userSavedRoleId)) {
                deleteRoleList.add(userSavedRoleId);
            }
        }
        //获取要添加的权限
        for (String roleId : roleList) {
            if (!userSavedRoleIdList.contains(roleId)) {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(user.getId());
                userRoleRelation.setRoleCode(roleId);
                userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
                userRoleRelation.setCreateUser(operator);
                userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
                saveList.add(userRoleRelation);
            }
        }
        if (CollectionUtils.isNotEmpty(deleteRoleList)) {
            List<String> deleteIdList = new ArrayList<>();
            userRoleRelationList.forEach(item -> {
                if (deleteRoleList.contains(item.getRoleCode())) {
                    deleteIdList.add(item.getId());
                }
            });
            QueryChain<UserRoleRelation> deleteExample = queryChain().where(USER_ROLE_RELATION.ID.in(deleteIdList));
            mapper.deleteByQuery(deleteExample);
            //记录删除日志
//            operationLogService.batchAdd(this.getBatchLogs(deleteRoleList, user, "updateUser", operator, OperationLogType.DELETE.name()));
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            //系统级权限不会太多，所以暂时不分批处理
            saveList.forEach(item -> mapper.insertSelective(item));
            //记录添加日志
//            operationLogService.batchAdd(this.getBatchLogs(saveList.stream().map(UserRoleRelation::getRoleId).toList(), user, "updateUser", operator, OperationLogType.ADD.name()));
        }
    }

    @Override
    public Map<String, UserTableResponse> selectGlobalUserRoleAndOrganization(List<String> userIdList) {
        List<UserRoleRelation> userRoleRelationList = queryChain().where(USER_ROLE_RELATION.USER_ID.in(userIdList)).list();
        List<String> userRoleIdList = userRoleRelationList.stream().map(UserRoleRelation::getRoleCode).distinct().toList();
        List<String> sourceIdList = userRoleRelationList.stream().map(UserRoleRelation::getSourceId).distinct().toList();
        Map<String, UserRole> userRoleMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(userRoleIdList)) {
            List<UserRole> userRoles = QueryChain.of(UserRole.class).where(USER_ROLE.CODE.in(userRoleIdList).and(USER_ROLE.SCOPE_ID.eq("global"))).list();
            userRoleMap = userRoles.stream().collect(Collectors.toMap(UserRole::getCode, item -> item));
        }
        Map<String, UserTableResponse> returnMap = new HashMap<>();
        for (UserRoleRelation userRoleRelation : userRoleRelationList) {
            UserTableResponse userInfo = returnMap.get(userRoleRelation.getUserId());
            if (userInfo == null) {
                userInfo = new UserTableResponse();
                userInfo.setId(userRoleRelation.getUserId());
                returnMap.put(userRoleRelation.getUserId(), userInfo);
            }
            UserRole userRole = userRoleMap.get(userRoleRelation.getRoleCode());
            if (userRole != null && userRole.getType().equalsIgnoreCase(UserRoleScope.SYSTEM)) {
                userInfo.setUserRole(userRole);
            }
        }
        return returnMap;
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOptionWithLimit(String roleId, String keyword) {
        List<UserExcludeOptionDTO> selectOptions = QueryChain.of(SystemUser.class)
                .select(SYSTEM_USER.ID, SYSTEM_USER.NAME, SYSTEM_USER.EMAIL).from(SYSTEM_USER)
                .where(SYSTEM_USER.NAME.like(keyword).or(SYSTEM_USER.EMAIL.like(keyword))).limit(1000)
                .listAs(UserExcludeOptionDTO.class);
        // 查询已经关联的用户ID
        Set<String> excludeUserIds = new HashSet<>(getUserIdByRoleId(roleId));
        // 标记已经关联的用户
        selectOptions.forEach(excludeOption -> {
            if (excludeUserIds.contains(excludeOption.getId())) {
                excludeOption.setDisabled(true);
            }
        });
        return selectOptions;
    }

    @Override
    public void deleteByUserIdList(List<String> userIdList) {
        QueryChain<UserRoleRelation> userRoleRelationQueryChain = queryChain().where(USER_ROLE_RELATION.USER_ID.in(userIdList));
        mapper.deleteByQuery(userRoleRelationQueryChain);
    }

    private void checkAdminPermissionRemove(String userId, String roleId) {
        if (Objects.equals(roleId, InternalUserRole.ADMIN.getValue()) && Objects.equals(userId, InternalUserRole.ADMIN.getValue())) {
            throw new CustomException(USER_ROLE_RELATION_REMOVE_ADMIN_USER_PERMISSION);
        }
    }
}
