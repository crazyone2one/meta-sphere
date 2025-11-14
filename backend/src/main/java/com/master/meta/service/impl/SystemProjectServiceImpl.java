package com.master.meta.service.impl;

import com.master.meta.dto.ProjectSwitchRequest;
import com.master.meta.dto.system.project.*;
import com.master.meta.dto.system.user.UserExtendDTO;
import com.master.meta.dto.system.user.UserRoleOptionDto;
import com.master.meta.entity.SystemProject;
import com.master.meta.entity.SystemUser;
import com.master.meta.handle.Translator;
import com.master.meta.handle.exception.CustomException;
import com.master.meta.handle.log.constants.OperationLogModule;
import com.master.meta.handle.log.constants.OperationLogType;
import com.master.meta.mapper.OrganizationMapper;
import com.master.meta.mapper.SystemUserMapper;
import com.master.meta.mapper.UserRoleRelationMapper;
import com.master.meta.service.OperationLogService;
import com.master.meta.service.SystemProjectService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.query.QueryMethods;
import com.mybatisflex.core.update.UpdateChain;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.master.meta.entity.table.OrganizationTableDef.ORGANIZATION;
import static com.master.meta.entity.table.SystemProjectTableDef.SYSTEM_PROJECT;
import static com.master.meta.entity.table.SystemUserTableDef.SYSTEM_USER;

/**
 * 项目 服务层实现。
 *
 * @author 11's papa
 * @since 2025-10-13
 */
@Service("systemProjectService")
public class SystemProjectServiceImpl extends CommonProjectServiceImpl implements SystemProjectService {

    private final static String PREFIX = "/system/project";
    private final static String ADD_PROJECT = PREFIX + "/add";
    private final static String UPDATE_PROJECT = PREFIX + "/update";
    private final static String REMOVE_PROJECT_MEMBER = PREFIX + "/remove-member/";
    private final static String ADD_MEMBER = PREFIX + "/add-member";

    public SystemProjectServiceImpl(OrganizationMapper organizationMapper,
                                    UserRoleRelationMapper userRoleRelationMapper,
                                    OperationLogService operationLogService,
                                    SystemUserMapper userMapper) {
        super(organizationMapper, userRoleRelationMapper, operationLogService, userMapper);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int add(SystemProject request, String createUser) {
        checkProjectExistByName(request);
        return mapper.insertSelective(request);
    }

    @Override
    public ProjectDTO add(AddProjectRequest addProjectDTO, String createUser) {
        return super.add(addProjectDTO, createUser, ADD_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemUser switchProject(ProjectSwitchRequest request, String currentUserId) {
        if (!Strings.CS.equals(currentUserId, request.getUserId())) {
            throw new CustomException("未经授权");
        }
        if (mapper.selectOneById(request.getProjectId()) == null) {
            throw new CustomException("<项目不存在>");
        }
        UpdateChain.of(SystemUser.class).set(SYSTEM_USER.LAST_PROJECT_ID, request.getProjectId())
                .where(SYSTEM_USER.ID.eq(request.getUserId())).update();
        return QueryChain.of(SystemUser.class).where(SYSTEM_USER.ID.eq(request.getUserId())).one();
    }

    @Override
    public List<SystemProject> listProject(String organizationId) {
        return queryChain().where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(organizationId)).list();
    }

    @Override
    public SystemProject checkProjectExit(String projectId) {
        return queryChain().where(SYSTEM_PROJECT.ID.eq(projectId)).oneOpt().orElseThrow(() -> new CustomException("<项目不存在>"));
    }

    @Override
    public ProjectDTO get(String id) {
        return super.get(id);
    }

    @Override
    public Page<ProjectDTO> getProjectPage(ProjectRequest request) {
        Page<ProjectDTO> page = queryChain()
                .select(SYSTEM_PROJECT.ALL_COLUMNS, ORGANIZATION.NAME.as("organization_name"))
                .from(SYSTEM_PROJECT).innerJoin(ORGANIZATION).on(SYSTEM_PROJECT.ORGANIZATION_ID.eq(ORGANIZATION.ID))
                .where(SYSTEM_PROJECT.ORGANIZATION_ID.eq(request.getOrganizationId())
                        .and(SYSTEM_PROJECT.NAME.like(request.getKeyword()).or(SYSTEM_PROJECT.NUM.like(request.getKeyword()))))
                .pageAs(new Page<>(request.getPage(), request.getPageSize()), ProjectDTO.class);
        super.buildUserInfo(page.getRecords());
        return page;
    }

    @Override
    public boolean update(UpdateProjectRequest request, String updateUser) {
        return super.update(request, updateUser, UPDATE_PROJECT, OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    public int delete(String id, String userName) {
        return super.delete(id, userName);
    }

    @Override
    public void enable(String id, String updateUser) {
        super.enable(id, updateUser);
    }

    @Override
    public void disable(String id, String updateUser) {
        super.disable(id, updateUser);
    }

    @Override
    public Page<UserExtendDTO> getProjectMember(ProjectMemberRequest request) {
        Page<UserExtendDTO> page = getProjectMemberList(request);
        if (page.hasRecords()) {
            List<UserExtendDTO> memberList = page.getRecords();
            List<String> userIds = memberList.stream().map(UserExtendDTO::getId).toList();
            List<UserRoleOptionDto> userRole = selectProjectUserRoleByUserIds(userIds, request.getProjectId());
            Map<String, List<UserRoleOptionDto>> roleMap = userRole.stream().collect(Collectors.groupingBy(UserRoleOptionDto::getUserId));
            memberList.forEach(user -> {
                if (roleMap.containsKey(user.getId())) {
                    user.setUserRoleList(roleMap.get(user.getId()));
                }
            });
        }
        return page;
    }

    @Override
    public void addMemberByProject(ProjectAddMemberRequest request, String createUser) {
        super.addProjectUser(request, createUser, ADD_MEMBER, OperationLogType.ADD.name(), Translator.get("add"), OperationLogModule.SETTING_SYSTEM_ORGANIZATION);
    }

    @Override
    public int removeProjectMember(String projectId, String userId, String userName) {
        return super.removeProjectMember(projectId, userId, userName, OperationLogModule.SETTING_SYSTEM_ORGANIZATION, StringUtils.join(REMOVE_PROJECT_MEMBER, projectId, "/", userId));
    }

    @Override
    public List<SystemUser> getUserList(String keyword) {
        return QueryChain.of(SystemUser.class)
                .select(QueryMethods.distinct(SYSTEM_USER.ID, SYSTEM_USER.NAME, SYSTEM_USER.EMAIL))
                .where(SYSTEM_USER.NAME.like(keyword).or(SYSTEM_USER.EMAIL.like(keyword)))
                .orderBy(SYSTEM_USER.CREATE_TIME, false).limit(1000)
                .list();
    }

    private void checkProjectExistByName(SystemProject request) {
        boolean exists = queryChain().where(SYSTEM_PROJECT.NAME.eq(request.getName())
                .and(SYSTEM_PROJECT.ORGANIZATION_ID.eq(request.getOrganizationId()))
                .and(SYSTEM_PROJECT.ID.ne(request.getId()))).exists();
        if (exists) {
            throw new CustomException("<项目名称已存在>");
        }
    }
}
