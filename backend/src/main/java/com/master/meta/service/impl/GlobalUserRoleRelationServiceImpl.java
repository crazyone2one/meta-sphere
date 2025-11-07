package com.master.meta.service.impl;

import com.master.meta.constants.UserRoleScope;
import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.system.UserExcludeOptionDTO;
import com.master.meta.dto.system.UserRoleRelationUpdateRequest;
import com.master.meta.entity.SystemUser;
import com.master.meta.entity.UserRole;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.service.BaseUserRoleService;
import com.master.meta.service.GlobalUserRoleRelationService;
import com.mybatisflex.core.query.QueryChain;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.handle.result.CommonResultCode.USER_ROLE_RELATION_EXIST;
import static com.master.meta.handle.result.SystemResultCode.GLOBAL_USER_ROLE_PERMISSION;
import static com.master.meta.handle.result.SystemResultCode.GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION;

/**
 * @author Created by 11's papa on 2025/11/7
 */
@Service("globalUserRoleRelationService")
public class GlobalUserRoleRelationServiceImpl extends BaseUserRoleRelationServiceImpl implements GlobalUserRoleRelationService {
    private final BaseUserRoleService baseUserRoleService;

    public GlobalUserRoleRelationServiceImpl(@Qualifier("baseUserRoleService") BaseUserRoleService baseUserRoleService) {
        this.baseUserRoleService = baseUserRoleService;
    }

    @Override
    public List<UserExcludeOptionDTO> getExcludeSelectOption(String roleCode, String keyword) {
        baseUserRoleService.getWithCheckByCode(roleCode);
        return super.getExcludeSelectOptionWithLimit(roleCode, keyword);
    }

    @Override
    public void add(UserRoleRelationUpdateRequest request) {
        checkGlobalSystemUserRoleLegality(Collections.singletonList(request.getCode()));
        checkUserLegality(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        request.getUserIds().forEach(userId -> {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            userRoleRelation.setRoleCode(request.getCode());
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(UserRoleScope.SYSTEM);
            checkExist(userRoleRelation);
            userRoleRelation.setOrganizationId(UserRoleScope.SYSTEM);
            userRoleRelations.add(userRoleRelation);
        });
        mapper.insertBatch(userRoleRelations);
    }

    private void checkUserLegality(List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        long count = QueryChain.of(SystemUser.class).where(SYSTEM_USER.ID.in(userIds)).count();
        if (count != userIds.size()) {
            throw new CustomException(Translator.get("user.id.not.exist"));
        }
    }

    private void checkExist(UserRoleRelation userRoleRelation) {
        boolean exists = queryChain().where(USER_ROLE_RELATION.USER_ID.eq(userRoleRelation.getUserId())
                .and(USER_ROLE_RELATION.ROLE_CODE.eq(userRoleRelation.getRoleCode()))).exists();
        if (exists) {
            throw new CustomException(USER_ROLE_RELATION_EXIST);
        }
    }

    private void checkGlobalSystemUserRoleLegality(List<String> checkIdList) {
        List<UserRole> userRoleList = baseUserRoleService.listByIds(checkIdList);
        if (userRoleList.size() != checkIdList.size()) {
            throw new CustomException(Translator.get("user_role_not_exist"));
        }
        userRoleList.forEach(userRole -> {
            if (!Strings.CI.equals(userRole.getType(), UserRoleType.SYSTEM.name())) {
                throw new CustomException(GLOBAL_USER_ROLE_RELATION_SYSTEM_PERMISSION);
            }
            if (!Strings.CI.equals(userRole.getScopeId(), UserRoleScope.GLOBAL)) {
                throw new CustomException(GLOBAL_USER_ROLE_PERMISSION);
            }
        });
    }
}
