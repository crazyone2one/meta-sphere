package com.master.meta.service.impl;

import com.master.meta.dto.system.project.ProjectAddMemberRequest;
import com.master.meta.dto.system.request.ProjectUserRequest;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.entity.UserRoleRelation;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.OperationLogService;
import com.master.meta.service.OrganizationProjectService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.master.meta.entity.table.OrganizationTableDef.ORGANIZATION;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;
import static com.master.meta.entity.table.UserRoleRelationTableDef.USER_ROLE_RELATION;

@Service("orgProjectService")
public class OrganizationProjectServiceImpl extends CommonProjectServiceImpl implements OrganizationProjectService {
    public OrganizationProjectServiceImpl(OrganizationMapper organizationMapper,
                                          UserRoleRelationMapper userRoleRelationMapper,
                                          OperationLogService operationLogService,
                                          SystemUserMapper userMapper) {
        super(organizationMapper, userRoleRelationMapper, operationLogService, userMapper);
    }

    private final static String PREFIX = "/organization-project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    @Override
    public Page<UserExtendDTO> getUserPage(ProjectUserRequest request) {
        checkOrgIsExist(request.getOrganizationId());
        checkProjectNotExist(request.getProjectId());
        List<UserRoleRelation> userRoleRelations = QueryChain.of(userRoleRelationMapper).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getOrganizationId())).list();
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        if (userIds.isEmpty()) {
            return new Page<>();
        } else {
            QueryChain<UserRoleRelation> userRoleRelationQueryChain = QueryChain.of(userRoleRelationMapper)
                    .select(USER_ROLE_RELATION.ID, USER_ROLE_RELATION.USER_ID)
                    .from(USER_ROLE_RELATION).where(USER_ROLE_RELATION.SOURCE_ID.eq(request.getProjectId()));
            return QueryChain.of(userMapper)
                    .select(QueryMethods.distinct(SYSTEM_USER.ID, SYSTEM_USER.NAME, SYSTEM_USER.EMAIL))
                    .select("count(temp.id) > 0 as memberFlag")
                    .from(SYSTEM_USER).as("u")
                    .leftJoin(userRoleRelationQueryChain).as("temp").on("temp.user_id = u.id")
                    .where(SYSTEM_USER.ID.in(userIds).and(SYSTEM_USER.NAME.like(request.getKeyword()).or(SYSTEM_USER.EMAIL.like(request.getKeyword()))))
                    .groupBy(SYSTEM_USER.ID)
                    .orderBy(SYSTEM_USER.CREATE_TIME.desc())
                    .pageAs(new Page<>(request.getPage(), request.getPageSize()), UserExtendDTO.class);
        }
    }

    private void checkOrgIsExist(String organizationId) {
        queryChain().where(ORGANIZATION.ID.eq(organizationId)).oneOpt()
                .orElseThrow(() -> new CustomException(Translator.get("organization_not_exists")));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void orgAddProjectMember(ProjectAddMemberRequest request, String createUser) {
        addProjectUser(request, createUser, ADD_MEMBER, OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_ORGANIZATION_PROJECT);
    }
}
