package com.master.meta.service.impl;

import com.master.meta.constants.HttpMethodConstants;
import com.master.meta.constants.OperationLogConstants;
import com.master.meta.constants.UserRoleEnum;
import com.master.meta.constants.UserRoleType;
import com.master.meta.dto.system.OptionDTO;
import com.master.meta.dto.system.request.MemberRequest;
import com.master.meta.dto.system.request.OrganizationMemberRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.entity.*;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.log.LogDTO;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.OperationLogService;
import com.master.meta.service.OrganizationService;
import com.master.meta.utils.JSON;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.OrganizationTableDef.ORGANIZATION;
import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;
import static com.master.meta.entity.table.UserRoleTableDef.USER_ROLE;

/**
 * 组织 服务层实现。
 *
 * @author 11's papa
 * @since 2025-11-05
 */
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {
    private final OperationLogService operationLogService;
    private final UserRoleRelationMapper userRoleRelationMapper;

    private static final String ADD_MEMBER_PATH = "/system/organization/add-member";
    private static final String REMOVE_MEMBER_PATH = "/system/organization/remove-member";
    public static final Integer DEFAULT_REMAIN_DAY_COUNT = 30;
    private static final Long DEFAULT_ORGANIZATION_NUM = 100001L;

    @Override
    public Page<UserExtendDTO> getMemberPage(MemberRequest request) {
        return queryChain()
                .select(QueryMethods.distinct(SYSTEM_USER.ID, SYSTEM_USER.NAME, SYSTEM_USER.EMAIL))
                .select("count(uur.id) > 0 as memberFlag")
                .from(SYSTEM_USER).as("u")
                .leftJoin(USER_ROLE_RELATION).as("uur").on(USER_ROLE_RELATION.USER_ID.eq(SYSTEM_USER.ID)
                        .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getSourceId())))
                .where(SYSTEM_USER.NAME.like(request.getKeyword()).or(SYSTEM_USER.EMAIL.like(request.getKeyword())))
                .groupBy(SYSTEM_USER.ID)
                .pageAs(new Page<>(request.getPage(), request.getPageSize()), UserExtendDTO.class);
    }

    @Override
    public List<OptionDTO> getUserRoleList(String organizationId) {
        //校验组织是否存在
        checkOrgExistById(organizationId);
        List<String> scopeIds = Arrays.asList(UserRoleEnum.GLOBAL.toString(), organizationId);
        List<UserRole> userRoles = QueryChain.of(UserRole.class)
                .where(USER_ROLE.SCOPE_ID.in(scopeIds).and(USER_ROLE.TYPE.eq(UserRoleType.ORGANIZATION.toString()))).list();
        return userRoles.stream().map(userRole -> new OptionDTO(userRole.getCode(), userRole.getName())).toList();
    }

    @Override
    public void addMemberBySystem(OrganizationMemberRequest request, String createUser) {
        List<LogDTO> logs = new ArrayList<>();
        addMemberAndGroup(request, createUser);
        List<String> nameList = QueryChain.of(SystemUser.class)
                .select(SYSTEM_USER.NAME)
                .where(SYSTEM_USER.ID.in(request.getUserIds())).listAs(String.class);
        setLog(request.getOrganizationId(), createUser, OperationLogType.ADD.name(), Translator.get("add") + Translator.get("organization_member_log") + ": " + StringUtils.join(nameList, ","), ADD_MEMBER_PATH, null, null, logs);
        operationLogService.batchAdd(logs);
    }

    @Override
    public Map<String, Long> getTotal(String organizationId) {
        Map<String, Long> total = new HashMap<>();
        if (StringUtils.isNotBlank(organizationId)) {
            total.put("projectTotal", QueryChain.of(SystemProject.class).where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(organizationId)).count());
            total.put("organizationTotal", 1L);
        } else {
            // 统计所有项目
            total.put("projectTotal", QueryChain.of(SystemProject.class).count());
            total.put("organizationTotal", queryChain().count());
        }
        return total;
    }

    @Override
    public List<OptionDTO> listOption(String currentUserId) {
        boolean exists = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(currentUserId)
                .and(USER_ROLE_RELATION.ROLE_CODE.eq("admin"))).exists();
        if (exists) {
            return queryChain().select(ORGANIZATION.ID, ORGANIZATION.NAME)
                    .where(ORGANIZATION.ENABLE.eq(true))
                    .listAs(OptionDTO.class);
        }
        List<String> relatedOrganizationIds = QueryChain.of(UserRoleRelation.class).select(QueryMethods.distinct(ORGANIZATION.ID))
                .from(USER_ROLE_RELATION).join(ORGANIZATION).on(ORGANIZATION.ID.eq(USER_ROLE_RELATION.ORGANIZATION_ID))
                .where(USER_ROLE_RELATION.USER_ID.eq(currentUserId).and(ORGANIZATION.ENABLE.eq(true))).listAs(String.class);
        if (CollectionUtils.isEmpty(relatedOrganizationIds)) {
            return List.of();
        }
        return queryChain().select(ORGANIZATION.ID, ORGANIZATION.NAME)
                .where(ORGANIZATION.ENABLE.eq(true).and(ORGANIZATION.ID.in(relatedOrganizationIds)))
                .listAs(OptionDTO.class);
    }

    private void addMemberAndGroup(OrganizationMemberRequest request, String createUser) {
        checkOrgExistByIds(List.of(request.getOrganizationId()));
        Map<String, SystemUser> userMap = checkUserExist(request.getUserIds());
        List<UserRoleRelation> userRoleRelations = new ArrayList<>();
        for (String userId : request.getUserIds()) {
            if (userMap.get(userId) == null) {
                throw new CustomException(Translator.get("user.not.exist") + ", id: " + userId);
            }
            long count = QueryChain.of(UserRoleRelation.class).where(USER_ROLE_RELATION.USER_ID.eq(userId)
                    .and(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId()))).count();
            if (count > 0) {
                continue;
            }
            request.getUserRoleIds().forEach(userRoleId -> {
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setUserId(userId);
                userRoleRelation.setRoleCode(userRoleId);
                userRoleRelation.setSourceId(request.getOrganizationId());
                userRoleRelation.setCreateUser(createUser);
                userRoleRelation.setOrganizationId(request.getOrganizationId());
                userRoleRelations.add(userRoleRelation);
            });
        }
        if (CollectionUtils.isNotEmpty(userRoleRelations)) {
            userRoleRelationMapper.insertBatch(userRoleRelations);
        }
    }

    private Map<String, SystemUser> checkUserExist(@NotEmpty(message = "{user.id.not_blank}") List<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            throw new CustomException(Translator.get("user.not.empty"));
        }
        List<SystemUser> users = QueryChain.of(SystemUser.class).where(SYSTEM_USER.ID.in(userIds)).list();
        if (CollectionUtils.isEmpty(users)) {
            throw new CustomException(Translator.get("user.not.exist"));
        }
        return users.stream().collect(Collectors.toMap(SystemUser::getId, user -> user));
    }

    private void checkOrgExistByIds(List<String> organizationId) {
        long count = queryChain().where(ORGANIZATION.ID.in(organizationId)).count();
        if (count != organizationId.size()) {
            throw new CustomException(Translator.get("organization_not_exist"));
        }
    }

    private void setLog(String organizationId, String createUser, String type, String content, String path, Object originalValue, Object modifiedValue, List<LogDTO> logs) {
        LogDTO dto = new LogDTO(
                OperationLogConstants.SYSTEM,
                OperationLogConstants.SYSTEM,
                organizationId,
                createUser,
                type,
                OperationLogModule.SETTING_SYSTEM_ORGANIZATION,
                content);
        dto.setPath(path);
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(originalValue));
        dto.setModifiedValue(JSON.toJSONBytes(modifiedValue));
        logs.add(dto);
    }

    private void checkOrgExistById(String organizationId) {
        queryChain().where(ORGANIZATION.ID.eq(organizationId)).oneOpt().orElseThrow(() -> new CustomException(Translator.get("organization_not_exist")));
    }
}
